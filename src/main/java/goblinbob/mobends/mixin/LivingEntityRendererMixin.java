package goblinbob.mobends.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.BendsRenderer;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * Poses the entities Mo' Bends animates as their render starts, and draws Mo' Bends' parts in
 * place of the vanilla model. The layers (armour, held items, cape) still draw as vanilla does.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    /**
     * The data of the entity this renderer is drawing, posed for this frame; null while it draws
     * one as vanilla does. A renderer draws one entity at a time, on the render thread.
     */
    @Unique
    @Nullable
    private DefinedEntityData<?> mobends$bent;

    /**
     * The pose as {@code setupRotations} starts, this render (the entity's position and scale), and
     * the body yaw vanilla turns it by: where 1.12's frame for the root and center bones is found again.
     */
    @Unique
    private final Matrix4f mobends$entityFrame = new Matrix4f();
    @Unique
    private float mobends$bodyYaw;

    @Inject(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD")
    )
    private void mobends$pose(LivingEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight, CallbackInfo ci) {
        mobends$bent = BendsRenderer.beforeRender(entity, partialTicks);
    }

    /** Notes the entity's frame and body yaw as vanilla turns the entity (see {@link #mobends$entityFrame}). */
    @WrapOperation(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V"
        )
    )
    private void mobends$keepEntityFrame(LivingEntityRenderer<?, ?> renderer, LivingEntity entity, PoseStack poseStack, float bob, float bodyYaw,
                                         float partialTicks, float scale, Operation<Void> original) {
        if (mobends$bent != null) {
            mobends$entityFrame.set(poseStack.last().pose());
            mobends$bodyYaw = bodyYaw;
        }
        original.call(renderer, entity, poseStack, bob, bodyYaw, partialTicks, scale);
    }

    /**
     * Draws the bent parts instead of the model, with what vanilla would draw the model with. Wrapped
     * rather than redirected, so other mods wrapping the same call still run.
     */
    @WrapOperation(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
        )
    )
    private void mobends$drawBent(EntityModel<?> model, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color,
                                  Operation<Void> original) {
        DefinedEntityData<?> bent = mobends$bent;
        if (bent != null && BendsRenderer.renderModel(bent, model, mobends$entityFrame, mobends$bodyYaw, poseStack, consumer, packedLight, packedOverlay, color)) {
            return;
        }
        original.call(model, poseStack, consumer, packedLight, packedOverlay, color);
    }
}
