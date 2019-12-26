package net.gobbob.mobends.core.math.vector;

public class VectorUtils
{

    /*-- Normalizing --*/

    public static void normalize(IVec3fRead v, IVec3f dest) throws IllegalArgumentException
    {
        float length = v.length();
        if (length == 0)
            throw new IllegalArgumentException("A zero vector cannot be normalized.");
        dest.set(v.getX() / length, v.getY() / length, v.getZ() / length);
    }

    public static void normalize(IVec3dRead v, IVec3d dest) throws IllegalArgumentException
    {
        double length = v.length();
        if (length == 0)
            throw new IllegalArgumentException("A zero vector cannot be normalized.");
        dest.set(v.getX() / length, v.getY() / length, v.getZ() / length);
    }

    public static void normalize(IVec3f vec) throws IllegalArgumentException
    {
        normalize(vec, vec);
    }

    public static void normalize(IVec3d vec) throws IllegalArgumentException
    {
        normalize(vec, vec);
    }

    public static Vec3f getNormalized(IVec3fRead src) throws IllegalArgumentException
    {
        Vec3f vec = new Vec3f();
        normalize(src, vec);
        return vec;
    }

    public static Vec3d getNormalized(IVec3dRead src) throws IllegalArgumentException
    {
        Vec3d vec = new Vec3d();
        normalize(src, vec);
        return vec;
    }

    public static Vec3f getScaled(IVec3fRead vec, float a)
    {
        return new Vec3f(vec.getX() * a, vec.getY() * a, vec.getZ() * a);
    }

    public static Vec3d getScaled(IVec3dRead vec, double a)
    {
        return new Vec3d(vec.getX() * a, vec.getY() * a, vec.getZ() * a);
    }

    public static float dot(IVec3fRead left, IVec3fRead right)
    {
        return left.getX() * right.getX() + left.getY() * right.getY() + left.getZ() * right.getZ();
    }

    public static double dot(IVec3dRead left, IVec3dRead right)
    {
        return left.getX() * right.getX() + left.getY() * right.getY() + left.getZ() * right.getZ();
    }

    public static IVec3f multiply(IVec3fRead left, IVec3fRead right, IVec3f dest)
    {
        dest.setX(left.getX() * right.getX());
        dest.setY(left.getY() * right.getY());
        dest.setZ(left.getZ() * right.getZ());
        return dest;
    }

    public static IVec3fRead cross(IVec3fRead left, IVec3fRead right, IVec3f dest)
    {
        dest.set(
                left.getY() * right.getZ() - left.getZ() * right.getY(),
                right.getX() * left.getZ() - right.getZ() * left.getX(),
                left.getX() * right.getY() - left.getY() * right.getX()
        );

        return dest;
    }

    public static IVec3fRead cross(float lx, float ly, float lz, float rx, float ry, float rz, IVec3f dest)
    {
        dest.set(
                ly * rz - lz * ry,
                rx * lz - rz * lx,
                lx * ry - ly * rx
        );

        return dest;
    }

    public static float distanceSq(IVec3fRead left, IVec3fRead right)
    {
        float dx = left.getX() - right.getX();
        float dy = left.getY() - right.getY();
        float dz = left.getZ() - right.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    public static float distance(IVec3fRead left, IVec3fRead right)
    {
        return (float) Math.sqrt(distanceSq(left, right));
    }

    public static void subtract(IVec3fRead a, IVec3fRead b, IVec3f dest)
    {
        dest.set(
                a.getX() - b.getX(),
                a.getY() - b.getY(),
                a.getZ() - b.getZ()
        );
    }

    public static void subtract(IVec3dRead a, IVec3dRead b, IVec3d dest)
    {
        dest.set(
                a.getX() - b.getX(),
                a.getY() - b.getY(),
                a.getZ() - b.getZ()
        );
    }

    public static Vec3f subtract(IVec3fRead a, IVec3fRead b)
    {
        Vec3f vec = new Vec3f();
        subtract(a, b, vec);
        return vec;
    }

    public static Vec3d subtract(IVec3dRead a, IVec3dRead b)
    {
        Vec3d vec = new Vec3d();
        subtract(a, b, vec);
        return vec;
    }

    public static void slerp(IVec3dRead a, IVec3dRead b, double t, IVec3d dest)
    {
        double cosAlpha = dot(a, b);
        double alpha = (float) Math.acos(cosAlpha);
        double sinAlpha = Math.sin(alpha);

        double t1 = Math.sin((1 - t) * alpha) / sinAlpha;
        double t2 = Math.sin(t * alpha) / sinAlpha;

        dest.set(a.getX() * t1 + b.getX() * t2,
                a.getY() * t1 + b.getY() * t2,
                a.getZ() * t1 + b.getZ() * t2);
    }

    public static Vec3d slerp(IVec3dRead a, IVec3dRead b, double t)
    {
        Vec3d vec = new Vec3d();
        slerp(a, b, t, vec);
        return vec;
    }

}
