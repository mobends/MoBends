package goblinbob.mobends.forge;

import goblinbob.mobends.core.IEntityDataRepository;
import goblinbob.mobends.core.IMutator;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.util.GUtil;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class ForgeMutator<D extends EntityData, E extends LivingEntity, M extends EntityModel<E>> implements IMutator<ForgeMutationContext>
{
    protected M vanillaModel;
    protected float headYaw;
    protected float headPitch;
    protected float limbSwing;
    protected float limbSwingAmount;
    protected float swingProgress;

    private final IEntityDataRepository<ForgeMutationContext> dataRepository;

    public ForgeMutator(IEntityDataRepository<ForgeMutationContext> dataRepository)
    {
        this.dataRepository = dataRepository;
    }

    @Override
    public boolean mutate(ForgeMutationContext context)
    {
        LivingEntity entity = context.getEntity();
        //noinspection unchecked
        LivingRenderer<E, M> renderer = (LivingRenderer<E, M>) context.getRenderer();
        float partialTicks = context.getPartialTicks();

        //noinspection unchecked
        this.updateModel((E) entity, renderer, partialTicks);
        //noinspection unchecked
        D data = (D) this.dataRepository.getOrMakeData(context);
        this.performAnimations(data, renderer, partialTicks);
        this.syncUpWithData(data);

        return false;
    }

    private void updateModel(E entity, LivingRenderer<?, ?> renderer, float partialTicks)
    {
        // The code below is a mirror of what in the LivingRenderer's render method.

        boolean shouldSit = entity.isPassenger() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
        float f = GUtil.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
        float f1 = GUtil.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
        float yaw = f1 - f;

        if (shouldSit && entity.getRidingEntity() instanceof LivingEntity)
        {
            LivingEntity livingentity = (LivingEntity) entity.getRidingEntity();
            f = GUtil.interpolateRotation(livingentity.prevRenderYawOffset, livingentity.renderYawOffset,
                    partialTicks);
            yaw = f1 - f;
            float f3 = MathHelper.wrapDegrees(yaw);

            if (f3 < -85.0F)
                f3 = -85.0F;
            if (f3 >= 85.0F)
                f3 = 85.0F;

            f = f1 - f3;

            if (f3 * f3 > 2500.0F)
                f += f3 * 0.2F;

            yaw = f1 - f;
        }

        float pitch = MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch);

        float f5 = 0.0F;
        float f6 = 0.0F;

        if (!shouldSit && entity.isAlive())
        {
            f5 = MathHelper.lerp(partialTicks, entity.prevLimbSwingAmount, entity.limbSwingAmount);
            f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

            if (entity.isChild())
                f6 *= 3.0F;
            if (f5 > 1.0F)
                f5 = 1.0F;
        }

        this.headYaw = yaw;
        this.headPitch = pitch;
        this.limbSwing = f6;
        this.limbSwingAmount = f5;
        this.swingProgress = entity.getSwingProgress(partialTicks);
    }

    private void performAnimations(D data, LivingRenderer<? extends E, ?> renderer, float partialTicks)
    {
        data.registerStoredProperty(BasePropertyKeys.HEAD_YAW, this.headYaw);
        data.registerStoredProperty(BasePropertyKeys.HEAD_PITCH, this.headPitch);
        data.registerStoredProperty(BasePropertyKeys.LIMB_SWING, this.limbSwing);
        data.registerStoredProperty(BasePropertyKeys.LIMB_SWING_AMOUNT, this.limbSwingAmount);
        data.registerStoredProperty(BasePropertyKeys.SWING_PROGRESS, this.swingProgress);

        // TODO Perform animations!
    }

    /**
     * Applying the virtual animation data to the actual entity.
     * @param data
     */
    private void syncUpWithData(EntityData data)
    {
        // TODO Implement this
    }
}
