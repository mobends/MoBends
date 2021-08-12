package goblinbob.mobends.standard.client.model.armor;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.PlayerData;
import goblinbob.mobends.standard.previewer.PlayerPreviewer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
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
                           headwearParts,
                           leftArmParts,
                           rightArmParts,
                           leftLegParts,
                           rightLegParts;

    protected ModelPartTransform bodyTransform;

    private static final IPartWrapper.ModelPartSetter bodySetter = (model, part) -> model.bipedBody = part;
    private static final IPartWrapper.ModelPartSetter headSetter = (model, part) -> model.bipedHead = part;
    private static final IPartWrapper.ModelPartSetter headwearSetter = (model, part) -> model.bipedHeadwear = part;
    private static final IPartWrapper.ModelPartSetter leftArmSetter = (model, part) -> model.bipedLeftArm = part;
    private static final IPartWrapper.ModelPartSetter rightArmSetter = (model, part) -> model.bipedRightArm = part;
    private static final IPartWrapper.ModelPartSetter leftLegSetter = (model, part) -> model.bipedLeftLeg = part;
    private static final IPartWrapper.ModelPartSetter rightLegSetter = (model, part) -> model.bipedRightLeg = part;

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

        this.bodyTransform = new ModelPartTransform();

        // Mutating by default, but not applying yet.
        this.partWrappers = new ArrayList<>();
        this.bodyParts = registerWrapper(original, bipedBody, bodySetter, data -> data.body).offsetInner(0, -12.0F, 0);
        this.headParts = registerWrapper(original, bipedHead, headSetter, data -> data.head)
            .setParent(this.bodyTransform);
        this.headwearParts = registerWrapper(original, bipedHeadwear, headwearSetter, data -> data.head)
            .setParent(this.bodyTransform);
        this.leftArmParts  = registerWrapper(original, bipedLeftArm,  leftArmSetter,  data -> data.leftArm,  data -> data.leftForeArm, 4.0F, 0.001F)
            .offsetLower(0, -4.0F, -2.0F).setParent(this.bodyTransform);
        this.rightArmParts = registerWrapper(original, bipedRightArm, rightArmSetter, data -> data.rightArm, data -> data.rightForeArm, 4.0F, 0.001F)
            .offsetLower(0, -4.0F, -2.0F).setParent(this.bodyTransform);
        this.leftLegParts  = registerWrapper(original, bipedLeftLeg,  leftLegSetter,  data -> data.leftLeg,  data -> data.leftForeLeg, 6.0F, 0F)
            .offsetLower(1.9F, -6.0F, 2.0F).offsetInner(1.9F, 0, 0);
        this.rightLegParts = registerWrapper(original, bipedRightLeg, rightLegSetter, data -> data.rightLeg, data -> data.rightForeLeg, 6.0F, 0F)
            .offsetLower(-1.9F, -6.0F, 2.0F).offsetInner(-1.9F, 0, 0);
    }

    private HumanoidPartWrapper registerWrapper(ModelBiped vanillaModel, ModelRenderer vanillaPart, IPartWrapper.ModelPartSetter setter, IPartWrapper.DataPartSelector dataSelector)
    {
        HumanoidPartWrapper wrapper = new HumanoidPartWrapper(vanillaModel, vanillaPart, setter, dataSelector);
        this.partWrappers.add(wrapper);
        return wrapper;
    }

    private HumanoidLimbWrapper registerWrapper(ModelBiped vanillaModel, ModelRenderer vanillaPart, IPartWrapper.ModelPartSetter setter, IPartWrapper.DataPartSelector data, IPartWrapper.DataPartSelector lowerData, float cutPlane, float inflation)
    {
        HumanoidLimbWrapper wrapper = new HumanoidLimbWrapper(vanillaModel, vanillaPart, setter, data, lowerData, cutPlane, inflation);
        this.partWrappers.add(wrapper);
        return wrapper;
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                       float headPitch, float scale)
    {
        if (!this.mutated)
        {
            throw new MalformedArmorModelException("Operating on a demutated armor wrapper.");
        }

        if (!(entityIn instanceof EntityLivingBase))
            return;
        EntityLivingBase entityLiving = (EntityLivingBase) entityIn;

        EntityBender<EntityLivingBase> entityBender = EntityBenderRegistry.instance.getForEntity(entityLiving);
        if (entityBender == null)
            return;

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

        this.apply();

        original.setModelAttributes(this);
        original.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

        this.deapply();
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scaleFactor, Entity entityIn)
    {
        // Do nothing
    }

    /**
     * Brings the original model back to it's vanilla state.
     */
    public void demutate()
    {
        this.deapply();
        this.partWrappers.clear();
        this.mutated = false;
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
