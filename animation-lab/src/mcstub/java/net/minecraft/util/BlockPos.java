package net.minecraft.util.math;

public class BlockPos extends Vec3i
{
    public BlockPos(int x, int y, int z) { super(x, y, z); }
    public BlockPos(double x, double y, double z) { super(x, y, z); }
    public BlockPos add(int x, int y, int z) { return new BlockPos(getX() + x, getY() + y, getZ() + z); }
    public BlockPos add(Vec3i vec) { return add(vec.getX(), vec.getY(), vec.getZ()); }
}
