package goblinbob.mobends.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.BendingEntityRenderer;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.BendsRenderer;
import goblinbob.mobends.core.client.VanillaDraw;
import goblinbob.mobends.core.client.preview.PreviewWorld;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.standard.client.renderer.entity.SwordTrail;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

/**
 * Poses the entities Mo' Bends animates as their render starts, and asks for Mo' Bends' parts to be
 * drawn in place of the vanilla model (by the model itself, see {@link BendsRenderer#markBentDraw}).
 * Each layer of a bent entity is drawn inside {@link BendsLayers#begin}, so the layers' mixins can
 * move what they draw onto the bent parts.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin implements BendingEntityRenderer {
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

    @Override
    @Nullable
    public DefinedEntityData<?> mobends$bentData() {
        return mobends$bent;
    }

    @Inject(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD")
    )
    private void mobends$pose(LivingEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight, CallbackInfo ci) {
        mobends$bent = BendsRenderer.beforeRender(entity, ((LivingEntityRenderer<?, ?>) (Object) this).getModel(), partialTicks);
    }

    /** A preview of the Mo' Bends menu has no name tag (the player's copy would show the player's name over it). */
    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void mobends$hidePreviewName(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (PreviewWorld.isPreview(entity)) {
            cir.setReturnValue(false);
        }
    }

    /** Draws a bent entity's sword trail, after the entity and its layers. */
    @Inject(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("TAIL")
    )
    private void mobends$drawTrail(LivingEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                                   CallbackInfo ci) {
        if (mobends$bent != null) {
            SwordTrail.draw(mobends$bent, mobends$entityFrame, mobends$bodyYaw, buffers);
        }
    }

    /** Notes the entity's frame and body yaw as vanilla turns the entity (see {@link #mobends$entityFrame}). */
    @WrapOperation(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(
            value = "INVOKE",
            //? if >=1.21 {
            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V"
            //?} else {
            /*target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V"
            *///?}
        )
    )
    //? if >=1.21 {
    private void mobends$keepEntityFrame(LivingEntityRenderer<?, ?> renderer, LivingEntity entity, PoseStack poseStack, float bob, float bodyYaw,
                                         float partialTicks, float scale, Operation<Void> original) {
    //?} else {
    /*private void mobends$keepEntityFrame(LivingEntityRenderer<?, ?> renderer, LivingEntity entity, PoseStack poseStack, float bob, float bodyYaw,
                                         float partialTicks, Operation<Void> original) {
    *///?}
        if (mobends$bent != null) {
            mobends$entityFrame.set(poseStack.last().pose());
            mobends$bodyYaw = bodyYaw;
        }
        //? if >=1.21 {
        original.call(renderer, entity, poseStack, bob, bodyYaw, partialTicks, scale);
        //?} else {
        /*original.call(renderer, entity, poseStack, bob, bodyYaw, partialTicks);
        *///?}
    }

    /**
     * Marks the model's draw as bent for the model's own {@code renderToBuffer} to take
     * (mixin.AgeableListModelMixin), and always goes on with the call: other mods wrapping it run as
     * usual, whatever their order, and the bent parts draw with what they finally pass the model.
     */
    @WrapOperation(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(
            value = "INVOKE",
            //? if >=1.21 {
            target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
            //?} else {
            /*target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"
            *///?}
        )
    )
    //? if >=1.21 {
    private void mobends$drawBent(EntityModel<?> model, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color,
                                  Operation<Void> original) {
    //?} else {
    /*private void mobends$drawBent(EntityModel<?> model, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha, Operation<Void> original) {
        int color = VanillaDraw.argb(red, green, blue, alpha);
    *///?}
        DefinedEntityData<?> bent = mobends$bent;
        if (bent == null) {
            //? if >=1.21 {
            original.call(model, poseStack, consumer, packedLight, packedOverlay, color);
            //?} else {
            /*original.call(model, poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
            *///?}
            return;
        }
        BendsRenderer.markBentDraw(bent, model, mobends$entityFrame, mobends$bodyYaw);
        try {
            //? if >=1.21 {
            original.call(model, poseStack, consumer, packedLight, packedOverlay, color);
            //?} else {
            /*original.call(model, poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
            *///?}
            // A model that draws its parts its own way didn't take the mark: drawn bent now.
            BendsRenderer.drawIfSkipped(model, poseStack, consumer, packedLight, packedOverlay, color);
        } finally {
            BendsRenderer.clearBentDraw();
        }
    }

    /**
     * Lets each layer of a bent entity know where the bent parts are ({@link BendsLayers#begin}),
     * while it draws. The layer always runs, as do other mods' wrappers around it.
     */
    @WrapOperation(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"
        )
    )
    private void mobends$renderLayerBent(RenderLayer<?, ?> layer, PoseStack poseStack, MultiBufferSource buffers, int packedLight, Entity entity,
                                         float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
                                         Operation<Void> original) {
        DefinedEntityData<?> bent = mobends$bent;
        if (bent == null) {
            original.call(layer, poseStack, buffers, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            return;
        }
        EntityModel<?> model = ((LivingEntityRenderer<?, ?>) (Object) this).getModel();
        BendsLayers.Frame outer = BendsLayers.begin(bent, model, layer, poseStack, mobends$entityFrame, mobends$bodyYaw);
        try {
            original.call(layer, poseStack, buffers, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        } finally {
            BendsLayers.end(outer);
        }
    }
}
