package goblinbob.mobends.mixin;

// 26.x has no list models: ModelFeatureRendererMixin draws every model.
//? if <26.1 {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.BendsRenderer;
import goblinbob.mobends.core.client.VanillaDraw;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ListModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The same as {@link AgeableListModelMixin}, for the models drawn as a plain list of parts
 * ({@code ListModel}: the shulker's), whose root {@link AgeableModelRootMixin} keeps. A subclass
 * that draws its own way (overriding {@code renderToBuffer}) isn't reached, and is drawn as vanilla.
 */
@Mixin(ListModel.class)
public abstract class ListModelMixin {
    //? if >=1.21 {
    @Inject(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"), cancellable = true)
    private void mobends$drawBent(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", at = @At("HEAD"), cancellable = true)
    private void mobends$drawBent(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha, CallbackInfo ci) {
        int color = VanillaDraw.argb(red, green, blue, alpha);
    *///?}
        EntityModel<?> model = (EntityModel<?>) (Object) this;
        if (BendsRenderer.drawMarked(model, poseStack, consumer, packedLight, packedOverlay, color)
                || BendsLayers.drawInLayer(model, poseStack, consumer, packedLight, packedOverlay, color)) {
            ci.cancel();
        }
    }
}
//?}
