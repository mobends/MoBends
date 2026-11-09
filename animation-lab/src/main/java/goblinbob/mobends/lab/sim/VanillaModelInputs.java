package goblinbob.mobends.lab.sim;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * What the renderer passes the vanilla model each frame, worked out as the mod captures it
 * ({@code BendsRenderer.captureRenderArguments}, after {@code LivingEntityRenderer.render}), but for
 * the entities named Dinnerbone or Grumm, which the lab has none of.
 */
public class VanillaModelInputs
{
    public float headYaw, headPitch, limbSwing, limbSwingAmount, swingProgress;

    public void compute(LivingEntity entity, float partialTicks)
    {
        boolean shouldSit = entity.isPassenger();
        float bodyYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = headYaw - bodyYaw;
        if (shouldSit && entity.getVehicle() instanceof LivingEntity mount)
        {
            // Riding, the body turns with the mount and the head is kept within 85 degrees of it.
            bodyYaw = Mth.rotLerp(partialTicks, mount.yBodyRotO, mount.yBodyRot);
            netHeadYaw = headYaw - bodyYaw;
            float clamped = Mth.clamp(Mth.wrapDegrees(netHeadYaw), -85.0F, 85.0F);
            bodyYaw = headYaw - clamped;
            if (clamped * clamped > 2500.0F)
            {
                bodyYaw += clamped * 0.2F;
            }
            netHeadYaw = headYaw - bodyYaw;
        }

        float headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        float limbSwingAmount = 0.0F;
        float limbSwing = 0.0F;
        if (!shouldSit && entity.isAlive())
        {
            limbSwingAmount = Math.min(entity.walkAnimation.speed(partialTicks), 1.0F);
            limbSwing = entity.walkAnimation.position(partialTicks);
            if (entity.isBaby())
            {
                limbSwing *= 3.0F;
            }
        }

        this.headYaw = netHeadYaw;
        this.headPitch = headPitch;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.swingProgress = entity.getAttackAnim(partialTicks);
    }
}
