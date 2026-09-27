package net.minecraft.util.math;

public class Vec3d
{
    public final double x, y, z;

    public Vec3d(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3d normalize()
    {
        double d0 = Math.sqrt(x * x + y * y + z * z);
        return d0 < 1.0E-4D ? new Vec3d(0, 0, 0) : new Vec3d(x / d0, y / d0, z / d0);
    }

    public Vec3d rotateYaw(float yaw)
    {
        float f = MathHelper.cos(yaw);
        float f1 = MathHelper.sin(yaw);
        double d0 = x * (double) f + z * (double) f1;
        double d2 = z * (double) f - x * (double) f1;
        return new Vec3d(d0, y, d2);
    }

    public double lengthVector() { return Math.sqrt(x * x + y * y + z * z); }
}
