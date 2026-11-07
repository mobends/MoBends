package net.minecraft.client.entity;

import net.minecraft.world.World;

/** Another player on a server. The lab never makes one; only what the animation code reads of it. */
public class EntityOtherPlayerMP extends AbstractClientPlayer
{
    public EntityOtherPlayerMP(World world) { super(world); }

    public boolean isCreative() { return false; }

    public boolean isSpectator() { return false; }

    public boolean isInLava() { return false; }
}
