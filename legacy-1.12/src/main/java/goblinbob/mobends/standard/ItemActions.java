package goblinbob.mobends.standard;

import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
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

    /** The vanilla arm pose an item in hand implies. */
    public static ModelBiped.ArmPose armPoseOf(EntityLivingBase entity, ItemStack heldItem)
    {
        if (!heldItem.isEmpty())
        {
            if (entity.getItemInUseCount() > 0)
            {
                EnumAction enumaction = heldItem.getItemUseAction();

                if (enumaction == EnumAction.BLOCK)
                    return ModelBiped.ArmPose.BLOCK;
                else if (enumaction == EnumAction.BOW)
                    return ModelBiped.ArmPose.BOW_AND_ARROW;
            }

            return ModelBiped.ArmPose.ITEM;
        }

        return ModelBiped.ArmPose.EMPTY;
    }

    public static UseActionType getBuiltInItemUseAction(Item item, ModelBiped.ArmPose armPoseMain, ModelBiped.ArmPose armPoseOff)
    {
        if (item == Items.AIR)
            return null;

        if (item instanceof ItemFood)
            return UseActionType.FOOD;

        if (item instanceof ItemBow || armPoseMain == ModelBiped.ArmPose.BOW_AND_ARROW || armPoseOff == ModelBiped.ArmPose.BOW_AND_ARROW)
            return UseActionType.BOW;

        if (armPoseMain == ModelBiped.ArmPose.BLOCK || armPoseOff == ModelBiped.ArmPose.BLOCK)
            return UseActionType.SHIELD;

        return UseActionType.FOOD;
    }

    public static UseActionType getItemUseAction(Item item, ModelBiped.ArmPose armPoseMain, ModelBiped.ArmPose armPoseOff)
    {
        UseActionType useActionType = ModConfig.getItemUseAction(item);

        return useActionType != null ? useActionType : getBuiltInItemUseAction(item, armPoseMain, armPoseOff);
    }

    public static AttackActionType getBuiltInItemAttackAction(Item item)
    {
        if (item instanceof ItemSword)
            return AttackActionType.SWORD;

        if (item == Items.AIR)
            return AttackActionType.FISTS;

        return AttackActionType.TOOL;
    }

    public static AttackActionType getItemAttackAction(Item item)
    {
        AttackActionType attackActionType = ModConfig.getItemAttackAction(item);

        return attackActionType != null ? attackActionType : getBuiltInItemAttackAction(item);
    }

}
