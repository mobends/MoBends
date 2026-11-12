package net.minecraft.util.math;

import net.minecraft.util.EnumFacing;

public class RayTraceResult
{
    public enum Type { MISS, BLOCK, ENTITY }

    public Type typeOfHit;
    public EnumFacing sideHit;
    public Vec3d hitVec;

    public RayTraceResult(Vec3d hitVec, EnumFacing sideHit)
    {
        this.typeOfHit = Type.BLOCK;
        this.hitVec = hitVec;
        this.sideHit = sideHit;
    }
}
