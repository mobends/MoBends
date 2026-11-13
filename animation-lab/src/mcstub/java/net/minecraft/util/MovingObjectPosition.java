package net.minecraft.util;

import net.minecraft.util.EnumFacing;

public class MovingObjectPosition
{
    public enum MovingObjectType { MISS, BLOCK, ENTITY }

    public MovingObjectType typeOfHit;
    public EnumFacing sideHit;
    public Vec3 hitVec;

    public MovingObjectPosition(Vec3 hitVec, EnumFacing sideHit)
    {
        this.typeOfHit = MovingObjectType.BLOCK;
        this.hitVec = hitVec;
        this.sideHit = sideHit;
    }
}
