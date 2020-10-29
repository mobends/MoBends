package goblinbob.mobends.standard.client.model.armor;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.model.ModelPartContainer;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.util.ModelUtils;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.PlayerData;
import goblinbob.mobends.standard.previewer.PlayerPreviewer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.util.vector.Vector3f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MutatedArmorModel extends ModelBiped
{

    protected ModelBiped original;
    protected List<Field> gatheredFields = new ArrayList<>();

    /**
     * Used to deapply the mutation of the armor.
     */
    protected HashMap<Field, ModelRenderer> fieldToOriginalMap = new HashMap<>();

    /**
     * Used to demutate the armor back into it's vanilla state.
     */
    protected HashMap<Field, ModelPartContainer> fieldToMutatedMap = new HashMap<>();

    /**
     * Keeps track of whether the model is mutated or not.
     */
    protected boolean mutated = false;

    /**
     * True if this armor model should be shown in the mutated form rather than the vanilla form.
     * This is useful if for example the Player wears an armor and is animated, but a skeleton who
     * is wearing the same armor isn't. In one instance the armor has to be shown as mutated, in the
     * other one as vanilla.
     */
    protected boolean applied = false;

    /**
     * This is used as a parent for other parts, like the arms and head.
     */
    protected ModelPartTransform mainBodyTransform;
    protected List<PartGroup<BipedEntityData<?>>> partGroups;

    protected PartGroup<BipedEntityData<?>> bodyParts;
    protected PartGroup<BipedEntityData<?>> headParts;
    protected PartGroup<BipedEntityData<?>> leftArmParts,
                                            rightArmParts;
    protected PartGroup<BipedEntityData<?>> leftLegParts,
                                            rightLegParts;
    protected PartGroup<BipedEntityData<?>> leftForeArmParts,
                                            rightForeArmParts;
    protected PartGroup<BipedEntityData<?>> leftForeLegParts,
                                            rightForeLegParts;

    protected AppendageSlicer appendageSlicer = new AppendageSlicer();

    public MutatedArmorModel(ModelBiped original)
    {
        this.original = original;
        this.mainBodyTransform = new ModelPartTransform();

        this.partGroups = new ArrayList<>();
        this.partGroups.add(this.bodyParts = new PartGroup<>(data -> data.body, model -> model.bipedBody));
        this.partGroups.add(this.headParts = new PartGroup<>(data -> data.head, model -> model.bipedHead));
        this.partGroups.add(this.leftArmParts = new PartGroup<>(data -> data.leftArm, model -> model.bipedLeftArm));
        this.partGroups.add(this.rightArmParts = new PartGroup<>(data -> data.rightArm, model -> model.bipedRightArm));
        this.partGroups.add(this.leftLegParts = new PartGroup<>(data -> data.leftLeg, model -> model.bipedLeftLeg));
        this.partGroups.add(this.rightLegParts = new PartGroup<>(data -> data.rightLeg, model -> model.bipedRightLeg));
        this.partGroups.add(this.leftForeArmParts = new PartGroup<>(data -> data.leftForeArm, model -> model.bipedLeftArm));
        this.partGroups.add(this.rightForeArmParts = new PartGroup<>(data -> data.rightForeArm, model -> model.bipedRightArm));
        this.partGroups.add(this.leftForeLegParts = new PartGroup<>(data -> data.leftForeLeg, model -> model.bipedLeftLeg));
        this.partGroups.add(this.rightForeLegParts = new PartGroup<>(data -> data.rightForeLeg, model -> model.bipedRightLeg));
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                       float headPitch, float scale)
    {
        this.apply();

        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

        if (!(entityIn instanceof EntityLivingBase))
            return;
        EntityLivingBase entityLiving = (EntityLivingBase) entityIn;

        EntityBender<EntityLivingBase> entityBender = EntityBenderRegistry.instance.getForEntity(entityLiving);
        if (entityBender == null)
            return;

        EntityData<?> entityData = EntityDatabase.instance.get(entityLiving);
        if (!(entityData instanceof BipedEntityData))
            return;

        final BipedEntityData<?> dataBiped = (BipedEntityData<?>) entityData;

        // Updating the visibility of children parts, so that they
        // match their original counterparts.
        this.updateVisibility();

        GlStateManager.pushMatrix();
        original.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

        if (entityIn.isSneaking())
        {
            // This value was fine-tuned to counteract the vanilla
            // translation done to the character.
            GlStateManager.translate(0, 0.2D, 0);
        }

        if (this.isChild)
        {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
        }

        renderPartGroups(leftForeArmParts, scale, dataBiped.body, dataBiped.leftArm);
        renderPartGroups(rightForeArmParts, scale, dataBiped.body, dataBiped.rightArm);
        renderPartGroups(leftForeLegParts, scale, dataBiped.leftLeg);
        renderPartGroups(rightForeLegParts, scale, dataBiped.rightLeg);

        GlStateManager.popMatrix();

        this.deapply();
    }

    private void renderPartGroups(PartGroup<BipedEntityData<?>> group, float scale, ModelPartTransform ...dependencies)
    {
        GlStateManager.pushMatrix();

        for (ModelPartTransform dependency : dependencies)
        {
            dependency.applyLocalTransform(scale);
        }

        group.getParts().forEach(part -> part.renderPart(scale));

        GlStateManager.popMatrix();
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scaleFactor, Entity entityIn)
    {
        original.setModelAttributes(this);

        if (!(entityIn instanceof EntityLivingBase))
            return;

        final EntityLivingBase entityLiving = (EntityLivingBase) entityIn;

        EntityData<?> entityData = EntityDatabase.instance.get(entityLiving);
        if (!(entityData instanceof BipedEntityData))
            return;

        if (entityData instanceof PlayerData && PlayerPreviewer.isPreviewInProgress())
        {
            entityData = PlayerPreviewer.getPreviewData();
        }

        final BipedEntityData<?> dataBiped = (BipedEntityData<?>) entityData;

        // Syncing up the model with animated data.
        this.mainBodyTransform.syncUp(dataBiped.body);
        this.partGroups.forEach(group -> group.syncUp(dataBiped));
    }

    protected void updateVisibility()
    {
        this.partGroups.forEach(group -> group.updateVisibility(this));
    }

    protected void mutate()
    {
        if (this.mutated)
        {
            this.demutate();
        }

        this.partGroups.forEach(PartGroup::clear);
        this.gatheredFields.clear();
        this.fieldToOriginalMap.clear();
        this.fieldToMutatedMap.clear();
        gatherFields(original.getClass());

        for (Field f : this.gatheredFields)
        {
            System.out.println("ArmorField: " + f);

            try
            {
                ModelRenderer modelRenderer = (ModelRenderer) f.get(original);

                if (modelRenderer != null)
                {
                    ModelPartContainer container;
                    if (modelRenderer instanceof ModelPartContainer)
                    {
                        container = (ModelPartContainer) modelRenderer;
                    }
                    else
                    {
                        System.out.println("Added to fieldToOriginalMap " + modelRenderer);
                        this.fieldToOriginalMap.put(f, modelRenderer);
                        this.appendageSlicer.registerOriginalModelRenderer(modelRenderer);
                        container = this.mutatePart(modelRenderer);
                    }
                    this.assignPart(container);
                    this.fieldToMutatedMap.put(f, container);
                }
            }
            catch (IllegalArgumentException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }

        this.sliceParts();
        this.positionParts();

        this.mutated = true;

        this.apply();
    }

    /**
     * Brings the original model back to it's vanilla state.
     */
    public void demutate()
    {
        this.deapply();

        this.gatheredFields.clear();
        this.fieldToOriginalMap.clear();
        this.partGroups.forEach(PartGroup::clear);

        this.mutated = false;
    }

    public void apply()
    {
        if (this.applied)
            return;

        // Assigning mutated models to fields in the armor model.
        for (Map.Entry<Field, ModelPartContainer> entry : fieldToMutatedMap.entrySet())
        {
            try
            {
                entry.getKey().set(original, entry.getValue());
            }
            catch (IllegalArgumentException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }

        this.appendageSlicer.apply();

        this.applied = true;
    }

    public void deapply()
    {
        if (!this.applied)
            return;

        // Assigning vanilla models to fields in the armor model.
        for (Map.Entry<Field, ModelRenderer> entry : fieldToOriginalMap.entrySet())
        {
            try
            {
                entry.getKey().set(original, entry.getValue());
            }
            catch (IllegalArgumentException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }

        this.appendageSlicer.deapply();

        this.applied = false;
    }

    /**
     * Used to get all the Fields from the modelClass and it's
     * superClasses that are an instance of ModelRenderer.
     */
    protected void gatherFields(Class<?> modelClass)
    {
        Field[] fields = modelClass.getDeclaredFields();
        for (Field f : fields)
        {
            if (!ModelRenderer.class.isAssignableFrom(f.getType()))
                continue;
            f.setAccessible(true);
            this.gatheredFields.add(f);
        }

        if (modelClass.getSuperclass() != null)
            gatherFields(modelClass.getSuperclass());
    }

    protected ModelPartContainer mutatePart(ModelRenderer modelRenderer)
    {
        ModelPartContainer container = new ModelPartContainer(this, modelRenderer);
        container.mirror = modelRenderer.mirror;

        return container;
    }

    protected void assignPart(ModelPartContainer container)
    {
        ModelRenderer part = container.getModel();
        ModelRenderer rootParent = ModelUtils.getRootParent(part, fieldToOriginalMap.values());
        if (rootParent == null)
            rootParent = part;
        Vector3f globalOrigin = ModelUtils.getGlobalOrigin(part, fieldToOriginalMap.values());

        AxisAlignedBB bounds = ModelUtils.getBounds(part);
        System.out.println("Bounds: " + bounds);

        if (globalOrigin.y >= 11F)
        {
            if (globalOrigin.x < 0F)
            {
                // Right leg/foreleg
                this.rightLegParts.add(container);
            }
            else
            {
                // Left leg/foreleg
                this.leftLegParts.add(container);
            }
        }
        else if (globalOrigin.x <= -5F || (part.cubeList != null && part.cubeList.size() > 0
                && part.rotationPointX + part.cubeList.get(0).posX1 <= -6F))
        {
            // Right arm/forearm
            this.rightArmParts.add(container);
        }
        else if (globalOrigin.x >= 5F || (part.cubeList != null && part.cubeList.size() > 0
                && part.rotationPointX + part.cubeList.get(0).posX2 >= 6F))
        {
            // Left arm/forearm
            this.leftArmParts.add(container);
        }
        else if (part.cubeList != null && part.cubeList.size() > 0 && bounds.maxY >= 4F)
        {
            // Body
            this.bodyParts.add(container);
        }
        else
        {
            // Head
            this.headParts.add(container);
        }
    }

    protected void positionParts()
    {
        headParts.forEach(part -> part.setParent(this.mainBodyTransform));
        bodyParts.forEach(part -> part.setInnerOffset(0, -12F, 0));

        leftArmParts.forEach(part -> {
            part.setInnerOffset(-5F, -2F, 0F);
            part.setParent(this.mainBodyTransform);
        });

        rightArmParts.forEach(part -> {
            part.setInnerOffset(5F, -2F, 0F);
            part.setParent(this.mainBodyTransform);
        });

        leftForeArmParts.forEach(part -> part.setInnerOffset(0F, -4.0F, -2F));
        rightForeArmParts.forEach(part -> part.setInnerOffset(0F, -4.0F, -2F));

        leftLegParts.forEach(part -> part.setInnerOffset(0F, -12F, 0F));
        rightLegParts.forEach(part -> part.setInnerOffset(0F, -12F, 0F));

        leftForeLegParts.forEach(part -> part.setInnerOffset(2F, -6F, 2F));
        rightForeLegParts.forEach(part -> part.setInnerOffset(-2F, -6F, 2F));
    }

    /**
     * This function takes groups of models, and divides them up into sub-groups,
     * like the upper arm and lower arm.
     */
    protected void sliceParts()
    {
        leftLegParts.forEach(part -> appendageSlicer.slice(this, part, leftForeLegParts, 18F));
        rightLegParts.forEach(part -> appendageSlicer.slice(this, part, rightForeLegParts, 18F));
        leftArmParts.forEach(part -> appendageSlicer.slice(this, part, leftForeArmParts, 6F));
        rightArmParts.forEach(part -> appendageSlicer.slice(this, part, rightForeArmParts, 6F));
    }

    public static MutatedArmorModel createFrom(ModelBiped src)
    {
        final MutatedArmorModel customModel = new MutatedArmorModel(src);
        customModel.mutate();

        return customModel;
    }

}
