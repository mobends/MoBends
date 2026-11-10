package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.BendsRenderer;
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
    @Inject(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"), cancellable = true)
    private void mobends$drawBent(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
        EntityModel<?> model = (EntityModel<?>) (Object) this;
        if (BendsRenderer.drawMarked(model, poseStack, consumer, packedLight, packedOverlay, color)
                || BendsLayers.drawInLayer(model, poseStack, consumer, packedLight, packedOverlay, color)) {
            ci.cancel();
        }
    }
}
