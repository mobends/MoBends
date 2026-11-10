package goblinbob.mobends.standard;

import goblinbob.mobends.core.configuration.ClientConfig;
import net.minecraft.client.model.HumanoidModel;
//? if >=1.21 {
import net.minecraft.core.component.DataComponents;
//?}
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
//? if <26.1 {
import net.minecraft.world.item.SwordItem;
//?}
import net.minecraft.world.item.UseAnim;

/**
 * Which use and attack action an item stands for (see {@code MoBendsOperations}): what the user's
 * config says of it (its {@code itemUseClassifications}, {@code itemAttackClassifications}), else
 * what its kind is.
 */
public final class ItemActions {
    private ItemActions() {
    }

    /** The vanilla arm pose an item in hand implies (the coarse one, before vanilla's per-item cases). */
    public static HumanoidModel.ArmPose armPoseOf(LivingEntity entity, ItemStack heldItem) {
        if (heldItem.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }
        if (entity.getUseItemRemainingTicks() > 0) {
            UseAnim action = heldItem.getUseAnimation();
            if (action == UseAnim.BLOCK) {
                return HumanoidModel.ArmPose.BLOCK;
            }
            if (action == UseAnim.BOW) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
        }
        return HumanoidModel.ArmPose.ITEM;
    }

    /**
     * How {@code stack}, the item in use, is used; null for no item, or one used in a way the
     * animators have no pose for.
     */
    public static UseActionType getItemUseAction(ItemStack stack, HumanoidModel.ArmPose armPoseMain, HumanoidModel.ArmPose armPoseOff) {
        if (stack.isEmpty()) {
            return null;
        }
        UseActionType configured = ClientConfig.INSTANCE.useActionOf(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        if (configured != null) {
            return configured;
        }
        UseAnim animation = stack.getUseAnimation();
        //? if >=1.21 {
        boolean food = stack.has(DataComponents.FOOD);
        //?} else {
        /*boolean food = stack.isEdible();
        *///?}
        if (food || animation == UseAnim.EAT || animation == UseAnim.DRINK) {
            return UseActionType.FOOD;
        }
        if (stack.getItem() instanceof BowItem || armPoseMain == HumanoidModel.ArmPose.BOW_AND_ARROW || armPoseOff == HumanoidModel.ArmPose.BOW_AND_ARROW) {
            return UseActionType.BOW;
        }
        if (armPoseMain == HumanoidModel.ArmPose.BLOCK || armPoseOff == HumanoidModel.ArmPose.BLOCK) {
            return UseActionType.SHIELD;
        }
        // 1.12 took anything else in use as food, as only food and drinks were left. The items added
        // since (spyglass, goat horn, brush, crossbow, trident) aren't eaten: none of the three fits
        // them, so they keep the pose the entity is otherwise in.
        return null;
    }

    /** How {@code stack}, the item in the main hand, attacks. */
    public static AttackActionType getItemAttackAction(ItemStack stack) {
        if (!stack.isEmpty()) {
            AttackActionType configured = ClientConfig.INSTANCE.attackActionOf(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            if (configured != null) {
                return configured;
            }
        }
        //? if >=26.1 {
        /*if (stack.is(net.minecraft.tags.ItemTags.SWORDS)) {
        *///?} else {
        if (stack.getItem() instanceof SwordItem) {
        //?}
            return AttackActionType.SWORD;
        }
        if (stack.isEmpty()) {
            return AttackActionType.FISTS;
        }
        return AttackActionType.TOOL;
    }
}
