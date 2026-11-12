package net.minecraft.util;

public class Vec3
{
    public final double xCoord, yCoord, zCoord;

    public Vec3(double x, double y, double z)
    {
        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;
    }

    public Vec3 normalize()
    {
        double d0 = Math.sqrt(xCoord * xCoord + yCoord * yCoord + zCoord * zCoord);
        return d0 < 1.0E-4D ? new Vec3(0, 0, 0) : new Vec3(xCoord / d0, yCoord / d0, zCoord / d0);
    }

    public Vec3 rotateYaw(float yaw)
    {
        float f = MathHelper.cos(yaw);
        float f1 = MathHelper.sin(yaw);
        double d0 = xCoord * (double) f + zCoord * (double) f1;
        double d2 = zCoord * (double) f - xCoord * (double) f1;
        return new Vec3(d0, yCoord, d2);
    }

    public double lengthVector() { return Math.sqrt(xCoord * xCoord + yCoord * yCoord + zCoord * zCoord); }
}
