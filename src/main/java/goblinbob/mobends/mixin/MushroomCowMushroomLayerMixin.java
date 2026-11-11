package goblinbob.mobends.mixin;

// Not ported to 26.x yet (the mobs).
//? if <26.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.BendsLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.MushroomCowMushroomLayer;
import net.minecraft.world.entity.animal.MushroomCow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Puts the mushroom on a bent mooshroom's head on the bent head ({@link BendsLayers#translateToPartOf}),
 * as 1.12's did through the model's {@code head}. The two on its back stay where vanilla puts them,
 * as they did in 1.12. (The mooshroom bends only with the dev pack's type file.)
 */
@Mixin(MushroomCowMushroomLayer.class)
public abstract class MushroomCowMushroomLayerMixin {
    @WrapOperation(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/MushroomCow;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void mobends$toBentHead(ModelPart head, PoseStack poseStack, Operation<Void> original, @Local(argsOnly = true) MushroomCow mooshroom) {
        if (!BendsLayers.translateToPartOf(mooshroom, "head", poseStack)) {
            original.call(head, poseStack);
        }
    }
}
//?}
