package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.math.Quaternion;

/** Quaternion blending helpers used by the pose pipeline. */
public class PoseMath
{

    /**
     * Normalised linear interpolation with hemisphere correction, so blending never takes the
     * long way round between q and -q.
     */
    public static void nlerp(Quaternion a, Quaternion b, float t, Quaternion dest)
    {
        float dot = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
        float sign = dot < 0 ? -1F : 1F;
        dest.set(a.x + (sign * b.x - a.x) * t,
                a.y + (sign * b.y - a.y) * t,
                a.z + (sign * b.z - a.z) * t,
                a.w + (sign * b.w - a.w) * t);
        dest.normalise();
    }

    /**
     * Scales a rotation: the result rotates about the same axis by {@code weight} times the
     * angle (slerp from identity). Exact for the single-axis rotations the procedural code builds.
     */
    public static void scale(Quaternion q, float weight, Quaternion dest)
    {
        if (weight == 1F)
        {
            dest.set(q);
            return;
        }
        if (weight == 0F)
        {
            dest.setIdentity();
            return;
        }

        float x = q.x, y = q.y, z = q.z, w = q.w;
        if (w < 0)
        {
            x = -x; y = -y; z = -z; w = -w;
        }
        float len = (float) Math.sqrt(x * x + y * y + z * z + w * w);
        if (len == 0)
        {
            dest.setIdentity();
            return;
        }
        x /= len; y /= len; z /= len; w /= len;

        float sinHalf = (float) Math.sqrt(x * x + y * y + z * z);
        if (sinHalf < 1e-7F)
        {
            dest.setIdentity();
            return;
        }
        float halfAngle = (float) Math.atan2(sinHalf, w);
        float scaledHalf = halfAngle * weight;
        float s = (float) Math.sin(scaledHalf) / sinHalf;
        dest.set(x * s, y * s, z * s, (float) Math.cos(scaledHalf));
    }

    public static void axisAngleDegrees(float x, float y, float z, float degrees, Quaternion dest)
    {
        dest.setFromAxisAngle(x, y, z, degrees / 180F * (float) Math.PI);
    }

    /** The rotation reflected across the YZ plane (a left-right mirror): axis x kept, y and z negated. */
    public static void mirrorX(Quaternion q)
    {
        q.y = -q.y;
        q.z = -q.z;
    }

}
