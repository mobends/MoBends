package net.minecraft.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** 1.8.9's: one held item (no off hand), equipment by number, no item use (only players use items) and no elytra. */
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
    /** 0 the held item, then the armour from the feet up. */
    public final ItemStack[] equipment = new ItemStack[5];
    public boolean playerSleeping;

    public EntityLivingBase(World world) { super(world); }

    public float getHealth() { return health; }
    public boolean isChild() { return child; }
    public ItemStack getHeldItem() { return equipment[0]; }
    public ItemStack getEquipmentInSlot(int slot) { return equipment[slot]; }
    public ItemStack getCurrentArmor(int slot) { return equipment[slot + 1]; }
    public void setCurrentItemOrArmor(int slot, ItemStack stack) { equipment[slot] = stack; }
    public boolean isPlayerSleeping() { return playerSleeping; }

    public float getSwingProgress(float partialTicks)
    {
        float f = swingProgress - prevSwingProgress;
        if (f < 0.0F) ++f;
        return prevSwingProgress + f * partialTicks;
    }
}
