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
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/^*
 * Puts the spyglass a bent player looks through at the bent head's eye, in place of the vanilla
 * head's frame ({@link BendsLayers#translateToHead}). 26.x moves to the model's root, then to its
 * head: on a bent player the first goes to the bent head, the second is left out.
 ^/
@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerMixin {
    @WrapOperation(
        method = "renderItemHeldToEye",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 0)
    )
    private void mobends$toBentHead(ModelPart root, PoseStack poseStack, Operation<Void> original, @Local(argsOnly = true) AvatarRenderState state,
                                    @Share("bentHead") LocalBooleanRef bentHead) {
        DefinedEntityData<?> bent = ((BentRenderState) state).mobends$bent();
        bentHead.set(bent != null && BendsLayers.translateToHead(bent.getEntity(), poseStack));
        if (!bentHead.get()) {
            original.call(root, poseStack);
        }
    }

    @WrapOperation(
        method = "renderItemHeldToEye",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 1)
    )
    private void mobends$skipVanillaHead(ModelPart head, PoseStack poseStack, Operation<Void> original, @Share("bentHead") LocalBooleanRef bentHead) {
        if (!bentHead.get()) {
            original.call(head, poseStack);
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
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Puts the spyglass a bent player looks through at the bent head's eye, in place of the vanilla
 * head's frame ({@link BendsLayers#translateToHead}).
 */
@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerMixin {
    @WrapOperation(
        method = "renderArmWithSpyglass(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void mobends$toBentHead(ModelPart head, PoseStack poseStack, Operation<Void> original, @Local(argsOnly = true) LivingEntity entity) {
        if (!BendsLayers.translateToHead(entity, poseStack)) {
            original.call(head, poseStack);
        }
    }
}
//?}
