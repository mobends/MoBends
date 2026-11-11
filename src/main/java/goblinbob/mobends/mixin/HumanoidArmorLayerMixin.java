package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.BendsLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/^*
 * Notes the armour piece the layer is about to submit ({@link BendsLayers#noteArmourPiece}), so a piece
 * the user keeps as vanilla draws as vanilla on a bent entity (settled as it's submitted, on 26.x).
 ^/
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {
    @WrapOperation(
        method = "submit",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V"
        )
    )
    private void mobends$notePiece(HumanoidArmorLayer<?, ?, ?> layer, PoseStack poseStack, SubmitNodeCollector collector, ItemStack stack,
                                   EquipmentSlot slot, int lightCoords, HumanoidRenderState state, Operation<Void> original) {
        BendsLayers.noteArmourPiece(stack);
        try {
            original.call(layer, poseStack, collector, stack, slot, lightCoords, state);
        } finally {
            BendsLayers.noteArmourPiece(ItemStack.EMPTY);
        }
    }
}
*///?} else {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import goblinbob.mobends.core.client.BendsLayers;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Notes the armour piece the layer is about to draw ({@link BendsLayers#noteArmourPiece}), so a piece
 * the user keeps as vanilla draws as vanilla on a bent entity. On NeoForge, vanilla's
 * {@code renderArmorPiece} hands over to one with more arguments, which reads the item.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {
    @WrapOperation(
        //? if neoforge {
        /*method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
        *///?} else {
        method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
        //?}
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack mobends$notePiece(LivingEntity entity, EquipmentSlot slot, Operation<ItemStack> original) {
        ItemStack stack = original.call(entity, slot);
        BendsLayers.noteArmourPiece(stack);
        return stack;
    }
}
//?}
