package goblinbob.mobends.mixin.geckolib;

// GeckoLib 5's bones (26.x); GeckoLib 4's armour is drawn by GeoArmorRendererMixin alone.
//? if >=26.1 {
/*import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.RenderPassInfo;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.standard.compat.geckolib.GeoArmorBones;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/^*
 * Draws the bones of a GeckoLib 5 armour piece that's drawn bent (see {@link GeoArmorBones}) in the
 * frame of the bent bone they're worn on: those GeckoLib's armour renderer draws on their own, and
 * those a re-render of the whole model (a render layer's) reaches as children. Every other bone
 * draws as GeckoLib draws it.
 ^/
@Pseudo
@Mixin(GeoBone.class)
public abstract class GeoBoneMixin {
    @WrapMethod(
        method = "positionAndRender(Lcom/geckolib/renderer/base/RenderPassInfo;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
    )
    private void mobends$drawBent(RenderPassInfo<?> info, VertexConsumer buffer, int packedLight, int packedOverlay, int colour,
                                  Operation<Void> original) {
        if (!GeoArmorBones.positionAndRender((GeoBone) (Object) this, info, buffer, packedLight, packedOverlay, colour)) {
            original.call(info, buffer, packedLight, packedOverlay, colour);
        }
    }

    @WrapMethod(
        method = "renderChildren(Lcom/geckolib/renderer/base/RenderPassInfo;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
    )
    private void mobends$drawChildrenBent(RenderPassInfo<?> info, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                                          int colour, Operation<Void> original) {
        if (!GeoArmorBones.renderChildren((GeoBone) (Object) this, info, poseStack, buffer, packedLight, packedOverlay, colour)) {
            original.call(info, poseStack, buffer, packedLight, packedOverlay, colour);
        }
    }
}
*///?}
