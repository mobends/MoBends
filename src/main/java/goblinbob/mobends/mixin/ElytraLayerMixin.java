package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.BentRenderState;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/^*
 * Hangs a bent entity's elytra from its bent body: 26.x's wings layer moves the elytra two pixels
 * back from the body (its only move) before it submits it, and here that happens in the bent body's
 * frame ({@link BendsLayers#translateToBody}). The wings still spread as vanilla's elytra model has them.
 ^/
@Mixin(WingsLayer.class)
public abstract class ElytraLayerMixin {
    @WrapOperation(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V")
    )
    private void mobends$fromBentBody(PoseStack poseStack, float x, float y, float z, Operation<Void> original,
                                      @Local(argsOnly = true) HumanoidRenderState state) {
        DefinedEntityData<?> bent = ((BentRenderState) state).mobends$bent();
        if (bent != null) {
            BendsLayers.translateToBody(bent.getEntity(), poseStack);
        }
        original.call(poseStack, x, y, z);
    }
}
*///?} else {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.BendsLayers;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hangs a bent entity's elytra from its bent body: vanilla's layer moves the elytra two pixels back
 * from the body (its only move), and here that happens in the bent body's frame
 * ({@link BendsLayers#translateToBody}). The wings still spread as vanilla's elytra model has them.
 */
@Mixin(ElytraLayer.class)
public abstract class ElytraLayerMixin {
    @WrapOperation(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V")
    )
    private void mobends$fromBentBody(PoseStack poseStack, float x, float y, float z, Operation<Void> original, @Local(argsOnly = true) LivingEntity entity) {
        BendsLayers.translateToBody(entity, poseStack);
        original.call(poseStack, x, y, z);
    }
}
//?}
