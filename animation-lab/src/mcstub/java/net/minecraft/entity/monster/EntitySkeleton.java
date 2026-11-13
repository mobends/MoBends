package net.minecraft.entity.monster;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntitySkeleton extends EntityLivingBase
{
    private int skeletonType;

    public EntitySkeleton(World world) { super(world); }
    public int getSkeletonType() { return skeletonType; }
    public void setSkeletonType(int type) { skeletonType = type; }
}
