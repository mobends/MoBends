package goblinbob.mobends.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.BendsLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Puts the potion a bent witch drinks at its bent head ({@link BendsLayers#translateToPartOf}),
 * then at its nose, which is drawn as vanilla's on the bent head; 1.12's did the same through the
 * model's {@code villagerHead}. What it holds otherwise stays where vanilla's crossed arms hold it,
 * as in 1.12.
 */
@Mixin(WitchItemLayer.class)
public abstract class WitchItemLayerMixin {
    @WrapOperation(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 0)
    )
    private void mobends$toBentHead(ModelPart head, PoseStack poseStack, Operation<Void> original, @Local(argsOnly = true) LivingEntity witch) {
        if (!BendsLayers.translateToPartOf(witch, "villagerHead", poseStack)) {
            original.call(head, poseStack);
        }
    }
}
