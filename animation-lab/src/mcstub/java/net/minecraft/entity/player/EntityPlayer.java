package net.minecraft.entity.player;

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

    public EntityPlayer(World world) { super(world); }

    public com.mojang.authlib.GameProfile getGameProfile() { return gameProfile; }

}
