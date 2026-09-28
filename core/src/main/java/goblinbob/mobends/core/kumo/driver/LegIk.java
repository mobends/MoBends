package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.math.Quaternion;

/**
 * Two-segment IK of a leg in model space (+Y down, -Z forward, right-handed rotations): the
 * rotations of the upper segment (relative to its parent) and of the lower one (relative to the
 * upper) that put the sole on a target. The joint only bends about the upper segment's X axis, the
 * leg is first turned about Y by a twist (which way the foot points), and the rest of the aim is
 * the smallest swing from there, so the knee keeps facing the way the foot points.
 */
public final class LegIk
{

    private LegIk()
    {
    }

    /**
     * @param knee   the lower segment's pivot relative to the upper's, at rest
     * @param foot   the sole relative to the lower segment's pivot, at rest
     * @param bend   1 if the joint bends by a positive angle about X (a knee), -1 if by a negative one (an elbow)
     * @param twist  radians the leg is turned by about Y before it is aimed
     * @param tx     the sole's target relative to the upper segment's pivot
     * @param upper  receives the upper segment's rotation
     * @param lower  receives the lower segment's rotation
     * @return the joint's angle in radians
     */
    public static double solve(float[] knee, float[] foot, float bend, double twist, double tx, double ty, double tz, Quaternion upper, Quaternion lower)
    {
        final double ux = knee[0], uy = knee[1], uz = knee[2];
        final double wx = foot[0], wy = foot[1], wz = foot[2];

        // |knee + rotX(k)·foot|² = |knee|² + |foot|² + 2·(ux·wx + A·cos k + B·sin k)
        final double a = uy * wy + uz * wz;
        final double b = uz * wy - uy * wz;
        final double r = Math.sqrt(a * a + b * b);
        final double distSq = tx * tx + ty * ty + tz * tz;
        double angle = 0;
        if (r > 1e-6)
        {
            final double k = (distSq - (ux * ux + uy * uy + uz * uz) - (wx * wx + wy * wy + wz * wz) - 2 * ux * wx) / 2;
            final double phase = Math.atan2(b, a);
            final double spread = Math.acos(Math.max(-1, Math.min(1, k / r)));
            // Of the two bends that reach, the smaller one that doesn't bend the joint backwards;
            // out of reach, the leg stays at its rest bend.
            angle = pickBend(wrapPi(phase + spread), wrapPi(phase - spread), bend);
        }

        final double cosK = Math.cos(angle), sinK = Math.sin(angle);
        // The sole relative to the hip with the joint bent, then twisted.
        final double ex = ux + wx;
        final double ey = uy + wy * cosK - wz * sinK;
        final double ez = uz + wy * sinK + wz * cosK;
        final double cosT = Math.cos(twist), sinT = Math.sin(twist);
        final double sx = ex * cosT + ez * sinT;
        final double sz = -ex * sinT + ez * cosT;

        // The shortest swing from where the twisted leg points to the target.
        final double sLen = Math.sqrt(sx * sx + ey * ey + sz * sz);
        final double tLen = Math.sqrt(distSq);
        double qx = 0, qy = 0, qz = 0, qw = 1;
        if (sLen > 1e-6 && tLen > 1e-6)
        {
            final double px = sx / sLen, py = ey / sLen, pz = sz / sLen;
            final double dx = tx / tLen, dy = ty / tLen, dz = tz / tLen;
            final double dot = px * dx + py * dy + pz * dz;
            if (dot < -0.999999)
            {
                // Pointing the opposite way: half a turn about X.
                qx = 1; qy = 0; qz = 0; qw = 0;
            }
            else
            {
                qx = py * dz - pz * dy;
                qy = pz * dx - px * dz;
                qz = px * dy - py * dx;
                qw = 1 + dot;
                final double len = Math.sqrt(qx * qx + qy * qy + qz * qz + qw * qw);
                qx /= len; qy /= len; qz /= len; qw /= len;
            }
        }

        // upper = swing · twist, the twist being about Y.
        final double ty2 = Math.sin(twist / 2), tw2 = Math.cos(twist / 2);
        upper.set((float) (qx * tw2 - qz * ty2),
                (float) (qy * tw2 + qw * ty2),
                (float) (qz * tw2 + qx * ty2),
                (float) (qw * tw2 - qy * ty2));
        lower.set((float) Math.sin(angle / 2), 0, 0, (float) Math.cos(angle / 2));
        return angle;
    }

    private static double pickBend(double first, double second, float bend)
    {
        final boolean firstAllowed = first * bend >= -1e-6;
        final boolean secondAllowed = second * bend >= -1e-6;
        if (firstAllowed && secondAllowed)
        {
            return Math.abs(first) <= Math.abs(second) ? first : second;
        }
        if (firstAllowed)
        {
            return first;
        }
        return secondAllowed ? second : 0;
    }

    private static double wrapPi(double angle)
    {
        angle %= Math.PI * 2;
        if (angle > Math.PI) angle -= Math.PI * 2;
        if (angle <= -Math.PI) angle += Math.PI * 2;
        return angle;
    }

}
