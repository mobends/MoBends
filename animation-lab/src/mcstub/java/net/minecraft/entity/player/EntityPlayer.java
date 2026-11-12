package net.minecraft.entity.player;

import net.minecraft.item.ItemStack;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntityPlayer extends EntityLivingBase
{
    public final PlayerCapabilities capabilities = new PlayerCapabilities();
    public double chasingPosX, chasingPosY, chasingPosZ;
    public double prevChasingPosX, prevChasingPosY, prevChasingPosZ;
    public float cameraYaw, prevCameraYaw;
    public float distanceWalkedModified, prevDistanceWalkedModified;

    public com.mojang.authlib.GameProfile gameProfile = new com.mojang.authlib.GameProfile(java.util.UUID.fromString("00000000-0000-0000-0000-00000000ab1e"), "LabPlayer");

    /** The item being used and the ticks of use left (1.8.9 keeps item use on the player). */
    public ItemStack itemInUse;
    public int itemInUseCount;
    /** The ticks the item has been used for. */
    public int itemInUseDuration;

    public EntityPlayer(World world) { super(world); }

    public ItemStack getItemInUse() { return itemInUse; }
    public int getItemInUseCount() { return itemInUseCount; }
    /** The game's is 0 unless an item is in use; the lab's scripts may use one with an empty hand. */
    public int getItemInUseDuration() { return itemInUseDuration; }
    public boolean isUsingItem() { return itemInUse != null; }

    public com.mojang.authlib.GameProfile getGameProfile() { return gameProfile; }

}
