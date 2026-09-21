package goblinbob.mobends.lab.sim;

import goblinbob.mobends.core.util.GUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

/**
 * A verbatim port of the interpolation in {@code Mutator.updateModel}, which mirrors what
 * RenderLivingBase feeds into vanilla models each frame.
 */
public class VanillaModelInputs
{
    public float headYaw, headPitch, limbSwing, limbSwingAmount, swingProgress;

    public void compute(EntityLivingBase entity, float partialTicks)
    {
        boolean shouldSit = entity.isRiding()
                && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
        float f = GUtil.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
        float f1 = GUtil.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
        float yaw = f1 - f;

        if (shouldSit && entity.getRidingEntity() instanceof EntityLivingBase)
        {
            EntityLivingBase entitylivingbase = (EntityLivingBase) entity.getRidingEntity();
            f = GUtil.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
            yaw = f1 - f;
            float f3 = MathHelper.wrapDegrees(yaw);

            if (f3 < -85.0F) f3 = -85.0F;
            if (f3 >= 85.0F) f3 = 85.0F;

            f = f1 - f3;

            if (f3 * f3 > 2500.0F) f += f3 * 0.2F;

            yaw = f1 - f;
        }

        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        float f5 = 0.0F;
        float f6 = 0.0F;

        if (!entity.isRiding())
        {
            f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
            f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

            if (entity.isChild()) f6 *= 3.0F;
            if (f5 > 1.0F) f5 = 1.0F;
            yaw = f1 - f;
        }

        this.headYaw = yaw;
        this.headPitch = pitch;
        this.limbSwing = f6;
        this.limbSwingAmount = f5;
        this.swingProgress = entity.getSwingProgress(partialTicks);
    }
}
