package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.BentRenderState;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/^* Puts a bent entity's held item in the bent hand ({@link BendsLayers#translateToHand}), in place of the vanilla arm's frame. ^/
@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {
    @WrapOperation(
        method = "submitArmWithItem",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void mobends$toBentHand(ArmedModel<?> model, EntityRenderState state, HumanoidArm arm, PoseStack poseStack, Operation<Void> original) {
        DefinedEntityData<?> bent = state instanceof BentRenderState bentState ? bentState.mobends$bent() : null;
        if (bent == null || !BendsLayers.translateToHand(bent.getEntity(), arm, poseStack)) {
            original.call(model, state, arm, poseStack);
        }
    }
}
*///?} else {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.BendsLayers;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Puts a bent entity's held items in its bent hands: through the arm and the forearm, turned in the
 * hand by the animator ({@link BendsLayers#translateToHand}), in place of the vanilla arm's frame.
 * The rest of vanilla's placing of the item (its turn into the hand, the offsets) stays.
 */
@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {
    @WrapOperation(
        method = "renderArmWithItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void mobends$toBentHand(ArmedModel model, HumanoidArm arm, PoseStack poseStack, Operation<Void> original,
                                    @Local(argsOnly = true) LivingEntity entity) {
        if (!BendsLayers.translateToHand(entity, arm, poseStack)) {
            original.call(model, arm, poseStack);
        }
    }
}
//?}
