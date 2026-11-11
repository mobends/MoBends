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
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/^*
 * Puts what a bent entity wears on its head (a skull, a carved pumpkin, a banner) on the bent head
 * ({@link BendsLayers#translateToHead}). 26.x moves to the model's root, then to its head: on a bent
 * entity the first goes to the bent head, the second is left out.
 ^/
@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin {
    @WrapOperation(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void mobends$toBentHead(ModelPart root, PoseStack poseStack, Operation<Void> original, @Local(argsOnly = true) LivingEntityRenderState state,
                                    @Share("bentHead") LocalBooleanRef bentHead) {
        DefinedEntityData<?> bent = ((BentRenderState) state).mobends$bent();
        bentHead.set(bent != null && BendsLayers.translateToHead(bent.getEntity(), poseStack));
        if (!bentHead.get()) {
            original.call(root, poseStack);
        }
    }

    @WrapOperation(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HeadedModel;translateToHead(Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void mobends$skipVanillaHead(HeadedModel model, PoseStack poseStack, Operation<Void> original, @Share("bentHead") LocalBooleanRef bentHead) {
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
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Puts what a bent entity wears on its head that isn't a helmet (a skull, a carved pumpkin, a
 * banner, any block or item) on the bent head ({@link BendsLayers#translateToHead}), in place of
 * the vanilla head's frame.
 */
@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin {
    @WrapOperation(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void mobends$toBentHead(ModelPart head, PoseStack poseStack, Operation<Void> original, @Local(argsOnly = true) LivingEntity entity) {
        if (!BendsLayers.translateToHead(entity, poseStack)) {
            original.call(head, poseStack);
        }
    }
}
//?}
