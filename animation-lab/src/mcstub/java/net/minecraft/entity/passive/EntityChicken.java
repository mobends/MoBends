package net.minecraft.entity.passive;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntityChicken extends EntityLivingBase
{
    public float wingRotation, destPos, oFlapSpeed, oFlap;
    public EntityChicken(World world) { super(world); }
}
