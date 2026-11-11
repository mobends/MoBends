package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.BentRenderState;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/^*
 * Puts the potion a bent witch drinks at its bent head ({@link BendsLayers#translateToPartOf}),
 * then at its nose, which is drawn as vanilla's on the bent head. 26.x moves to the model's root,
 * then to its head: on a bent witch the first goes to the bent head, the second is left out. What
 * it holds otherwise stays where vanilla's crossed arms hold it, as in 1.12.
 ^/
@Mixin(WitchItemLayer.class)
public abstract class WitchItemLayerMixin {
    @WrapOperation(
        method = "applyTranslation(Lnet/minecraft/client/renderer/entity/state/WitchRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 0)
    )
    private void mobends$toBentHead(ModelPart root, PoseStack poseStack, Operation<Void> original, @Local(argsOnly = true) WitchRenderState state,
                                    @Share("bentHead") LocalBooleanRef bentHead) {
        DefinedEntityData<?> bent = ((BentRenderState) state).mobends$bent();
        bentHead.set(bent != null && BendsLayers.translateToPartOf(bent.getEntity(), "villagerHead", poseStack));
        if (!bentHead.get()) {
            original.call(root, poseStack);
        }
    }

    @WrapOperation(
        method = "applyTranslation(Lnet/minecraft/client/renderer/entity/state/WitchRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/monster/witch/WitchModel;translateToHead(Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void mobends$skipVanillaHead(WitchModel model, PoseStack poseStack, Operation<Void> original, @Share("bentHead") LocalBooleanRef bentHead) {
        if (!bentHead.get()) {
            original.call(model, poseStack);
        }
    }
}
*///?} else {
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
//?}
