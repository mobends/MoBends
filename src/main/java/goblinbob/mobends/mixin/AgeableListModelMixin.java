package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.BendsRenderer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws a model's bent parts in place of its own, when its renderer marked this draw as bent
 * ({@link BendsRenderer#markBentDraw}). This is the innermost point of the model's draw: whatever
 * other mods do around the renderer's call has already happened, and their consumer, light,
 * overlay and colour are the ones the bent parts draw with. {@code AgeableListModel} is where the
 * models the definitions bend ({@code HumanoidModel}, so {@code PlayerModel}) get their draw, and
 * so do the vanilla armour models, which a bent entity's armour layer bends with it
 * ({@link BendsLayers#drawArmour}).
 */
@Mixin(AgeableListModel.class)
public abstract class AgeableListModelMixin {
    @Inject(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"), cancellable = true)
    private void mobends$drawBent(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
        EntityModel<?> model = (EntityModel<?>) (Object) this;
        if (BendsRenderer.drawMarked(model, poseStack, consumer, packedLight, packedOverlay, color)
                || BendsLayers.drawArmour(model, poseStack, consumer, packedLight, packedOverlay, color)) {
            ci.cancel();
        }
    }
}
