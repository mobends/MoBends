package net.minecraft.world.phys;

import net.minecraft.util.Mth;

public class Vec3 {
    public static final Vec3 ZERO = new Vec3(0.0, 0.0, 0.0);

    public final double x, y, z;

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3 normalize() {
        double length = Math.sqrt(x * x + y * y + z * z);
        return length < 1.0E-5F ? ZERO : new Vec3(x / length, y / length, z / length);
    }

    /** Turned about the Y axis by {@code angle} radians, as the game turns it. */
    public Vec3 yRot(float angle) {
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        return new Vec3(x * cos + z * sin, y, z * cos - x * sin);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
