package goblinbob.mobends.standard;

import goblinbob.mobends.core.compat.ArmPose;
import goblinbob.mobends.core.compat.Living;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;

/**
 * Which use and attack action an item stands for (the {@code useActionType} and
 * {@code attackActionType} properties animators read); the config can override the built-in rules.
 */
public final class ItemActions
{

    private ItemActions()
    {
    }

    /** The vanilla arm pose an item in hand implies (a sword blocks in 1.8.9, as a shield does later). */
    public static ArmPose armPoseOf(EntityLivingBase entity, ItemStack heldItem)
    {
        if (!Living.isEmpty(heldItem))
        {
            if (Living.getItemInUseCount(entity) > 0)
            {
                EnumAction enumaction = heldItem.getItemUseAction();

                if (enumaction == EnumAction.BLOCK)
                    return ArmPose.BLOCK;
                else if (enumaction == EnumAction.BOW)
                    return ArmPose.BOW_AND_ARROW;
            }

            return ArmPose.ITEM;
        }

        return ArmPose.EMPTY;
    }

    public static UseActionType getBuiltInItemUseAction(Item item, ArmPose armPoseMain, ArmPose armPoseOff)
    {
        if (item == null)
            return null;

        if (item instanceof ItemFood)
            return UseActionType.FOOD;

        if (item instanceof ItemBow || armPoseMain == ArmPose.BOW_AND_ARROW || armPoseOff == ArmPose.BOW_AND_ARROW)
            return UseActionType.BOW;

        if (armPoseMain == ArmPose.BLOCK || armPoseOff == ArmPose.BLOCK)
            return UseActionType.SHIELD;

        return UseActionType.FOOD;
    }

    public static UseActionType getItemUseAction(Item item, ArmPose armPoseMain, ArmPose armPoseOff)
    {
        UseActionType useActionType = ModConfig.getItemUseAction(item);

        return useActionType != null ? useActionType : getBuiltInItemUseAction(item, armPoseMain, armPoseOff);
    }

    public static AttackActionType getBuiltInItemAttackAction(Item item)
    {
        if (item instanceof ItemSword)
            return AttackActionType.SWORD;

        if (item == null)
            return AttackActionType.FISTS;

        return AttackActionType.TOOL;
    }

    public static AttackActionType getItemAttackAction(Item item)
    {
        AttackActionType attackActionType = ModConfig.getItemAttackAction(item);

        return attackActionType != null ? attackActionType : getBuiltInItemAttackAction(item);
    }

}
