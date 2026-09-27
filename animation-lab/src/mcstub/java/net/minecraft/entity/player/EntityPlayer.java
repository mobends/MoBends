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
    public int ticksElytraFlying;

    public EntityPlayer(World world) { super(world); }

    public int getTicksElytraFlying() { return ticksElytraFlying; }
}
