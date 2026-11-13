package goblinbob.mobends.core.compat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * What 1.9 and later ask of any living entity and 1.8.9 only of players, or not at all: hands, using an item, the
 * elytra. Only players use items in 1.8.9, always with their one (right) hand.
 */
public final class Living
{

    private Living()
    {
    }

    /** Whether the stack is nothing (1.8.9 has no empty stack: an empty slot holds null). */
    public static boolean isEmpty(ItemStack stack)
    {
        return stack == null || stack.getItem() == null || stack.stackSize <= 0;
    }

    /** The stack's item, or null for no stack (1.12's empty stack holds air). */
    public static Item getItem(ItemStack stack)
    {
        return isEmpty(stack) ? null : stack.getItem();
    }

    /** What the hand holds, or null. The off hand holds nothing. */
    public static ItemStack getHeldItem(EntityLivingBase entity, EnumHand hand)
    {
        return hand == EnumHand.MAIN_HAND ? entity.getHeldItem() : null;
    }

    public static ItemStack getHeldItemMainhand(EntityLivingBase entity)
    {
        return entity.getHeldItem();
    }

    /** Always null: 1.8.9 has no off hand. */
    public static ItemStack getHeldItemOffhand(EntityLivingBase entity)
    {
        return null;
    }

    /** Always the right: 1.8.9 has no left-handed entities. */
    public static EnumHandSide getPrimaryHand(EntityLivingBase entity)
    {
        return EnumHandSide.RIGHT;
    }

    /** The hand using an item: always the main hand. */
    public static EnumHand getActiveHand(EntityLivingBase entity)
    {
        return EnumHand.MAIN_HAND;
    }

    /** The item being used (eaten, drawn, blocked with), or null. */
    public static ItemStack getActiveItemStack(EntityLivingBase entity)
    {
        return entity instanceof EntityPlayer ? ((EntityPlayer) entity).getItemInUse() : null;
    }

    /** Whether an item is being used. */
    public static boolean isHandActive(EntityLivingBase entity)
    {
        return entity instanceof EntityPlayer && ((EntityPlayer) entity).isUsingItem();
    }

    /** The ticks of use left, 0 when no item is used (1.12's {@code getItemInUseCount}). */
    public static int getItemInUseCount(EntityLivingBase entity)
    {
        return entity instanceof EntityPlayer ? ((EntityPlayer) entity).getItemInUseCount() : 0;
    }

    /** The ticks the item has been used for (1.12's {@code getItemInUseMaxCount}, which is misnamed). */
    public static int getItemInUseMaxCount(EntityLivingBase entity)
    {
        return entity instanceof EntityPlayer ? ((EntityPlayer) entity).getItemInUseDuration() : 0;
    }

    /** Always false: 1.8.9 has no elytra. */
    public static boolean isElytraFlying(EntityLivingBase entity)
    {
        return false;
    }

    /** Always 0: 1.8.9 has no elytra. */
    public static int getTicksElytraFlying(EntityLivingBase entity)
    {
        return 0;
    }

    /** What the entity holds or wears in the slot, or null. */
    public static ItemStack getItemStackFromSlot(EntityLivingBase entity, EntityEquipmentSlot slot)
    {
        return slot.getStack(entity);
    }

    public static boolean isCreative(EntityPlayer player)
    {
        return player.capabilities.isCreativeMode;
    }
}
