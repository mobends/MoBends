package net.minecraft.util;

public class Vec3i
{
    private final int x, y, z;
    public Vec3i(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
    public Vec3i(double x, double y, double z) { this(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z)); }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
}
