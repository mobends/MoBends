package goblinbob.mobends.standard.client.model.armor;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.PlayerData;
import goblinbob.mobends.standard.main.MoBends;
import goblinbob.mobends.standard.previewer.PlayerPreviewer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.util.ArrayList;
import java.util.List;

public class ArmorWrapper extends ModelBiped
{
    protected ModelBiped original;

    /**
     * Keeps track of whether the model is mutated or not.
     */
    protected boolean mutated = true;

    /**
     * True if this armor model should be shown in the mutated form rather than the vanilla form.
     * This is useful if for example the Player wears an armor and is animated, but a skeleton who
     * is wearing the same armor isn't. In one instance the armor has to be shown as mutated, in the
     * other one as vanilla.
     */
    protected boolean applied = false;

    protected List<IPartWrapper> partWrappers;

    protected IPartWrapper bodyParts,
                           headParts,
                           headwearParts;
    protected IPartWrapper leftArmParts,
                           rightArmParts,
                           leftLegParts,
                           rightLegParts;

    protected ModelPartTransform bodyTransform;

    private ArmorWrapper(ModelBiped original)
    {
        this.original = original;
        // Replacing our unused modelRenderer properties with the ones from
        // the original model, meaning that whenever some internal method changes
        // the visibility of a model we can get it from these.
        this.bipedBody = original.bipedBody;
        this.bipedHead = original.bipedHead;
        this.bipedLeftArm = original.bipedLeftArm;
        this.bipedRightArm = original.bipedRightArm;
        this.bipedLeftLeg = original.bipedLeftLeg;
        this.bipedRightLeg = original.bipedRightLeg;
        this.bipedHeadwear = original.bipedHeadwear;

        this.partWrappers = new ArrayList<>();
        this.bodyParts = registerWrapper(new HumanoidPartWrapper(data -> data.body, model -> model.bipedBody, (model, part) -> model.bipedBody = part));
        this.headParts = registerWrapper(new HumanoidPartWrapper(data -> data.head, model -> model.bipedHead, (model, part) -> model.bipedHead = part));
        this.headwearParts = registerWrapper(new HumanoidPartWrapper(data -> data.head, model -> model.bipedHeadwear, (model, part) -> model.bipedHeadwear = part));
        this.leftArmParts = registerWrapper(new HumanoidLimbWrapper(data -> data.leftArm, data -> data.leftForeArm, model -> model.bipedLeftArm, (model, part) -> model.bipedLeftArm = part));
        this.rightArmParts = registerWrapper(new HumanoidLimbWrapper(data -> data.rightArm, data -> data.rightForeArm, model -> model.bipedRightArm, (model, part) -> model.bipedRightArm = part));
        this.leftLegParts = registerWrapper(new HumanoidLimbWrapper(data -> data.leftLeg, data -> data.leftForeLeg, model -> model.bipedLeftLeg, (model, part) -> model.bipedLeftLeg = part));
        this.rightLegParts = registerWrapper(new HumanoidLimbWrapper(data -> data.rightLeg, data -> data.rightForeLeg, model -> model.bipedRightLeg, (model, part) -> model.bipedRightLeg = part));

        this.bodyTransform = new ModelPartTransform();

        // Mutating by default, but not applying yet.
        for (IPartWrapper partWrapper : partWrappers)
        {
            partWrapper.mutate(original);
        }

        this.bodyParts.offsetInner(0, -12.0F, 0);
        this.headParts.setParent(this.bodyTransform);
        this.headwearParts.setParent(this.bodyTransform);
        this.leftArmParts.setParent(this.bodyTransform);
        this.rightArmParts.setParent(this.bodyTransform);

        this.leftLegParts.offsetInner(1.9F, 0, 0);
        this.rightLegParts.offsetInner(-1.9F, 0, 0);
    }

    private IPartWrapper registerWrapper(IPartWrapper wrapper)
    {
        this.partWrappers.add(wrapper);
        return wrapper;
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                       float headPitch, float scale)
    {
        if (!this.mutated)
        {
            throw new MalformedArmorModel("Operating on a demutated armor wrapper.");
        }

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

        GlStateManager.popMatrix();

        this.deapply();
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
        this.bodyTransform.syncUp(dataBiped.body);
        this.partWrappers.forEach(group -> group.syncUp(dataBiped));
    }

    /**
     * Brings the original model back to it's vanilla state.
     */
    public void demutate()
    {
        this.deapply();
        this.partWrappers.clear();
        this.mutated = false;

        MoBends.LOG.info("Demutating the armor");
    }

    public void apply()
    {
        if (this.applied)
            return;

        // Assigning mutated models to fields in the armor model.
        for (IPartWrapper wrapper : partWrappers)
        {
            wrapper.apply(this);
        }

        this.applied = true;
    }

    public void deapply()
    {
        if (!this.applied)
            return;

        // Assigning vanilla models to fields in the armor model.
        for (IPartWrapper wrapper : partWrappers)
        {
            wrapper.deapply(this);
        }

        this.applied = false;
    }

    public static ArmorWrapper createFor(ModelBiped src)
    {
        return new ArmorWrapper(src);
    }
}
