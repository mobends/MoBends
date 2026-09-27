package net.minecraft.entity;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.world.World;

public class EntityLivingBase extends Entity
{
    public float renderYawOffset, prevRenderYawOffset;
    public float rotationYawHead, prevRotationYawHead;
    public float limbSwing, limbSwingAmount, prevLimbSwingAmount;
    public float swingProgress, prevSwingProgress;
    public boolean isSwingInProgress;
    public int swingProgressInt;

    public float health = 20.0F;
    public boolean child;
    public EnumHandSide primaryHand = EnumHandSide.RIGHT;
    public ItemStack mainHand = ItemStack.EMPTY;
    public ItemStack offHand = ItemStack.EMPTY;
    public EnumHand activeHand = EnumHand.MAIN_HAND;
    public int itemInUseCount;
    public int itemInUseMaxCount;
    public boolean playerSleeping;

    public EntityLivingBase(World world) { super(world); }

    public float getHealth() { return health; }
    public boolean isChild() { return child; }
    public EnumHandSide getPrimaryHand() { return primaryHand; }
    public ItemStack getHeldItem(EnumHand hand) { return hand == EnumHand.MAIN_HAND ? mainHand : offHand; }
    public ItemStack getHeldItemMainhand() { return mainHand; }
    public ItemStack getHeldItemOffhand() { return offHand; }
    public EnumHand getActiveHand() { return activeHand; }
    public ItemStack getActiveItemStack() { return itemInUseCount > 0 ? getHeldItem(activeHand) : ItemStack.EMPTY; }
    public int getItemInUseCount() { return itemInUseCount; }
    public int getItemInUseMaxCount() { return itemInUseMaxCount; }
    public boolean isPlayerSleeping() { return playerSleeping; }

    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slot)
    {
        switch (slot)
        {
            case MAINHAND: return mainHand;
            case OFFHAND: return offHand;
            default: return ItemStack.EMPTY;
        }
    }

    public float getSwingProgress(float partialTicks)
    {
        float f = swingProgress - prevSwingProgress;
        if (f < 0.0F) ++f;
        return prevSwingProgress + f * partialTicks;
    }
}
