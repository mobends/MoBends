package goblinbob.mobends.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.definition.DefinedEntities;
import goblinbob.mobends.core.client.definition.DefinedModel;
import goblinbob.mobends.core.client.definition.DefinedRenderer;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.vanilla.VanillaNames;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4fc;

import javax.annotation.Nullable;

/**
 * Where a living entity's render meets Mo' Bends ({@code LivingEntityRendererMixin}): picks the
 * entity's definition, gets its data, and poses it for this frame from what the renderer is about
 * to pass the model; then draws Mo' Bends' parts for the model in its place.
 */
public final class BendsRenderer {
    /*
     * The bent draw the renderer has asked for, from just before it calls the model's renderToBuffer
     * until that call returns: the model's own renderToBuffer takes it and draws the bent parts
     * instead (mixin.AgeableListModelMixin). Drawing there, rather than in place of the renderer's
     * call, lets every other mod's wrapper around that call run, and draws with whatever consumer,
     * light, overlay and colour they pass on. Render thread only; a renderer draws one entity at a time.
     */
    @Nullable
    private static DefinedEntityData<?> markedData;
    @Nullable
    private static EntityModel<?> markedModel;
    @Nullable
    private static Matrix4fc markedEntityFrame;
    private static float markedBodyYaw;

    private BendsRenderer() {
    }

    /**
     * Asks for {@code model}'s next draw to be bent, as {@code data} poses it. Clear the mark with
     * {@link #clearBentDraw()} once the renderer's call to the model returns, however it returns, so
     * a draw of the same model elsewhere (another mod's preview, say) isn't bent.
     */
    public static void markBentDraw(DefinedEntityData<?> data, EntityModel<?> model, Matrix4fc entityFrame, float bodyYaw) {
        markedData = data;
        markedModel = model;
        markedEntityFrame = entityFrame;
        markedBodyYaw = bodyYaw;
    }

    public static void clearBentDraw() {
        markedData = null;
        markedModel = null;
        markedEntityFrame = null;
    }

    /**
     * Draws {@code model} bent if the renderer marked this draw of it, in place of the model's own
     * {@code renderToBuffer}. The mark is used up, so the model draws as vanilla if it's drawn
     * again before the renderer's call returns.
     *
     * @return true if it drew, and the model's own draw must be skipped
     */
    public static boolean drawMarked(EntityModel<?> model, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color) {
        DefinedEntityData<?> data = markedData;
        Matrix4fc entityFrame = markedEntityFrame;
        if (data == null || entityFrame == null || markedModel != model) {
            return false;
        }
        clearBentDraw();
        return renderModel(data, model, entityFrame, markedBodyYaw, poseStack, consumer, packedLight, packedOverlay, color);
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
     * Draws {@code model} bent, as the data poses it, in place of vanilla's {@code renderToBuffer}
     * (the same pose stack, consumer, light, overlay and colour).
     *
     * @param entityFrame the pose as the renderer's {@code setupRotations} starts, this render
     * @param bodyYaw     the body yaw vanilla turns the entity by
     * @return false if the model isn't one the definition bends, to draw it as vanilla does
     */
    private static boolean renderModel(DefinedEntityData<?> data, EntityModel<?> model, Matrix4fc entityFrame, float bodyYaw, PoseStack poseStack,
                                      VertexConsumer consumer, int packedLight, int packedOverlay, int color) {
        EntityModelDefinition definition = data.getDefinition();
        if (definition.model != null) {
            Class<?> modelClass = VanillaNames.modelClass(definition.model);
            if (modelClass == null || !modelClass.isInstance(model)) {
                return false;
            }
        }
        DefinedModel parts = DefinedModel.of(definition, model);
        if (parts == null) {
            return false;
        }
        // The positions read off this model: the entity's other model (a player's slim one, once
        // the skin is known) has its own.
        if (!data.hasAdoptedPositionsOf(parts)) {
            data.adoptPositions(parts, parts.positions());
        }
        poseStack.pushPose();
        DefinedRenderer.transformModel(data, model, poseStack, entityFrame, bodyYaw);
        parts.render(data, poseStack, consumer, packedLight, packedOverlay, color);
        poseStack.popPose();
        return true;
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
