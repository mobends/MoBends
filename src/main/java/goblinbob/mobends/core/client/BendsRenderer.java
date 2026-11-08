package goblinbob.mobends.core.client;

import goblinbob.mobends.core.client.definition.DefinedEntities;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * Where a living entity's render meets Mo' Bends ({@code LivingEntityRendererMixin}): picks the
 * entity's definition, gets its data, and poses it for this frame from what the renderer is about
 * to pass the model.
 */
public final class BendsRenderer {
    private BendsRenderer() {
    }

    /**
     * Poses {@code entity} for this frame, at the start of its render.
     *
     * @return its data, with the pose to draw; null to draw it as vanilla does (no definition picks
     *         it, it isn't in the world, or its animator asks for vanilla right now)
     */
    @Nullable
    public static DefinedEntityData<?> beforeRender(LivingEntity entity, float partialTicks) {
        DefinedEntities.DefinedEntity defined = DefinedEntities.select(entity);
        if (defined == null) {
            return null;
        }
        LivingEntityData<LivingEntity> data = EntityDatabase.instance.getOrMake(defined.dataFactory(), entity);
        if (!(data instanceof DefinedEntityData<?> definedData)) {
            return null;
        }
        captureRenderArguments(entity, partialTicks, definedData);
        definedData.animate();
        return definedData.wantsVanilla() ? null : definedData;
    }

    /**
     * Reads what {@code LivingEntityRenderer.render} passes the model's {@code setupAnim} (the head's
     * yaw and pitch, the limb swing and its amount) and the swing progress, as it computes them,
     * into the data the animator reads.
     */
    private static void captureRenderArguments(LivingEntity entity, float partialTicks, LivingEntityData<?> data) {
        // Vanilla's test (NeoForge also asks the vehicle, which hardly any does differently).
        boolean shouldSit = entity.isPassenger();
        float bodyYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = headYaw - bodyYaw;
        if (shouldSit && entity.getVehicle() instanceof LivingEntity mount) {
            // Riding, the body turns with the mount and the head is kept within 85 degrees of it.
            bodyYaw = Mth.rotLerp(partialTicks, mount.yBodyRotO, mount.yBodyRot);
            netHeadYaw = headYaw - bodyYaw;
            float clamped = Mth.clamp(Mth.wrapDegrees(netHeadYaw), -85.0F, 85.0F);
            bodyYaw = headYaw - clamped;
            if (clamped * clamped > 2500.0F) {
                bodyYaw += clamped * 0.2F;
            }
            netHeadYaw = headYaw - bodyYaw;
        }

        float headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        if (LivingEntityRenderer.isEntityUpsideDown(entity)) {
            headPitch *= -1.0F;
            netHeadYaw *= -1.0F;
        }

        float limbSwingAmount = 0.0F;
        float limbSwing = 0.0F;
        if (!shouldSit && entity.isAlive()) {
            limbSwingAmount = Math.min(entity.walkAnimation.speed(partialTicks), 1.0F);
            limbSwing = entity.walkAnimation.position(partialTicks);
            if (entity.isBaby()) {
                limbSwing *= 3.0F;
            }
        }

        data.headYaw = Mth.wrapDegrees(netHeadYaw);
        data.headPitch = Mth.wrapDegrees(headPitch);
        data.limbSwing = limbSwing;
        data.limbSwingAmount = limbSwingAmount;
        data.swingProgress = entity.getAttackAnim(partialTicks);
    }
}
