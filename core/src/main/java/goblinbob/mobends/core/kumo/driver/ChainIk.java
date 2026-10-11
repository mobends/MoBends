package goblinbob.mobends.core.kumo.driver;

/**
 * IK of a chain of two or three segments, in the frame the chain hangs from (model units: +Y down,
 * -Z forward, right-handed rotations), for {@code core:two_bone_ik} and {@code core:three_bone_ik}.
 *
 * <p>A joint is a hinge about the X axis of the segment before it, as a split's joints are (a
 * knee, an elbow), so the segments stay joined at their hinge; the first segment turns freely.
 * Each joint points where it is told. A two-segment chain bends its joint to the target's
 * distance, then turns about the line to the target until the joint points its way. A
 * three-segment chain first puts its second joint where it points, as far from the first pivot as
 * the first two segments would be bent by as much as the whole chain is, then solves the first two
 * for it; where the two joints are told to point out of one plane, the middle segment twists about
 * its own length (which keeps its hinges joined) so the last joint is a hinge too.
 *
 * <p>Rotations are {@code {x, y, z, w}} quaternions, in doubles.
 */
public final class ChainIk
{

    private static final double EPSILON = 1e-9;
    /** How many times a chain of three is aimed again to make up for what its end misses by. */
    private static final int REFINEMENTS = 8;

    private ChainIk()
    {
    }

    /**
     * @param segments   each segment at rest, in its own frame: from its pivot to the next one's, the last's to its end
     * @param bends      per segment, the sign the joint at its pivot bends by about X (1 a knee, -1 an elbow); the first's is unused
     * @param directions per segment, where the joint at its pivot points, in the chain's frame (of any length), or null; the first's is unused
     * @param fallbacks  per segment, where its joint points when its direction is null or along the reach (as it is posed so far)
     * @param target     the end's target, relative to the first pivot, in the chain's frame
     * @param rotations  receives each segment's rotation, relative to the segment before it (the first's to the chain's frame)
     * @return how far the end stays short of the target (0 where it reaches)
     */
    public static double solve(double[][] segments, float[] bends, double[][] directions, double[][] fallbacks, double[] target, double[][] rotations)
    {
        if (segments.length == 2)
        {
            double angle = bendFor(segments[0], segments[1], bends[1], dot(target, target));
            double[] end = add(segments[0], rotateX(segments[1], angle));
            aim(end, protrusion(end, bends[1]), target, directions[1], fallbacks[1], rotations[0]);
            axisAngleX(angle, rotations[1]);
            return Math.max(0, length(target) - length(end));
        }
        if (segments.length != 3)
        {
            throw new IllegalArgumentException("A chain has two or three segments.");
        }
        final double distance = length(target);
        final double total = length(segments[0]) + length(segments[1]) + length(segments[2]);
        // A sideways end can't always twist round to where the second joint was put for it, and
        // falls a little short: aimed past the target by as much, it reaches.
        double[] aim = target.clone();
        // The last joint's hinge, which the end's sideways part lies along: first guessed from where
        // that joint points (it bends about it to stick out that way), then as the chain was solved.
        final double[] along = normalized(target);
        double[] side = along == null ? null : across(directions[2], along);
        if (side == null && along != null)
        {
            side = across(fallbacks[2], along);
        }
        double[] hinge = side == null ? new double[] {1, 0, 0} : scaled(cross(along, side), -bends[2]);
        double[] end = solveThree(segments, bends, directions, fallbacks, aim, hinge, rotations);
        for (int i = 0; i < REFINEMENTS; i++)
        {
            final double[] miss = distance < total ? sub(target, end) : new double[3];
            final double[] solvedHinge = rotate(rotations[0], rotate(rotations[1], new double[] {1, 0, 0}));
            final double[] turned = sub(solvedHinge, hinge);
            if (dot(miss, miss) < 1e-14 && dot(turned, turned) < 1e-14)
            {
                break;
            }
            aim = add(aim, miss);
            hinge = solvedHinge;
            end = solveThree(segments, bends, directions, fallbacks, aim, hinge, rotations);
        }

        // What is left: the whole chain swung onto the target.
        final double[] swing = new double[4];
        if (shortestArc(end, target, swing))
        {
            multiply(swing, rotations[0], rotations[0]);
        }
        return Math.max(0, distance - length(end));
    }

    /**
     * Solves a chain of three for {@code target}, the last joint's hinge taken to be {@code hinge}
     * (in the chain's frame), and returns where its end is.
     */
    private static double[] solveThree(double[][] segments, float[] bends, double[][] directions, double[][] fallbacks, double[] target, double[] hinge,
                                       double[][] rotations)
    {
        final double[] r0 = segments[0], r1 = segments[1], r2 = segments[2];
        final double l0 = length(r0), l1 = length(r1), l2 = length(r2);
        final double total = l0 + l1 + l2;
        final double distance = length(target);

        // The second joint: the first two segments bent by as much as the whole chain, and the last
        // one's length from the target, where it points. The last segment's sideways part stays
        // along the hinge, so the joint is put for the target without it.
        final double reach = total > EPSILON ? (l0 + l1) * Math.min(distance, total) / total : 0;
        final double toSecond = Math.max(Math.abs(l0 - l1), Math.min(l0 + l1, reach));
        final double[] inPlane = sub(target, scaled(hinge, r2[0]));
        final double[] second = place(inPlane, toSecond, Math.sqrt(r2[1] * r2[1] + r2[2] * r2[2]), directions[2], fallbacks[2]);

        // The first two segments reach it.
        final double angle1 = bendFor(r0, r1, bends[1], dot(second, second));
        final double[] firstTwo = add(r0, rotateX(r1, angle1));
        aim(firstTwo, protrusion(firstTwo, bends[1]), second, directions[1], fallbacks[1], rotations[0]);

        // The target from the second joint, in the middle segment's frame before it twists.
        final double[] middle = new double[4];
        axisAngleX(angle1, middle);
        multiply(rotations[0], middle, middle);
        final double[] joint = add(rotate(rotations[0], r0), rotate(middle, r1));
        final double[] toTarget = rotateInverse(middle, sub(target, joint));
        final double toTargetLength = length(toTarget);
        if (toTargetLength > EPSILON)
        {
            scale(toTarget, l2 / toTargetLength);
        }

        // Twisting about its own length keeps the second joint where it is: the bend of the last
        // joint that leaves the last segment as much along that length as the target is, and the
        // twist that then turns it onto the target.
        final double[] axis = normalized(r1);
        double angle2 = 0;
        if (axis != null)
        {
            final double a = axis[1] * r2[1] + axis[2] * r2[2];
            final double b = axis[2] * r2[1] - axis[1] * r2[2];
            final double r = Math.sqrt(a * a + b * b);
            if (r > 1e-6)
            {
                final double k = dot(toTarget, axis) - axis[0] * r2[0];
                final double phase = Math.atan2(b, a);
                final double spread = Math.acos(clamp(k / r));
                angle2 = LegIk.pickBend(LegIk.wrapPi(phase + spread), LegIk.wrapPi(phase - spread), bends[2]);
            }
        }
        final double[] last = rotateX(r2, angle2);
        double twist = 0;
        if (axis != null)
        {
            final double[] from = sub(last, scaled(axis, dot(last, axis)));
            final double[] to = sub(toTarget, scaled(axis, dot(toTarget, axis)));
            if (length(from) > 1e-6 && length(to) > 1e-6)
            {
                twist = Math.atan2(dot(axis, cross(from, to)), dot(from, to));
            }
        }
        axisAngleX(angle1, rotations[1]);
        if (axis != null)
        {
            final double[] twisted = new double[4];
            axisAngle(axis, twist, twisted);
            multiply(rotations[1], twisted, rotations[1]);
        }
        axisAngleX(angle2, rotations[2]);
        return rotate(rotations[0], add(r0, rotate(rotations[1], add(r1, last))));
    }

    /**
     * The angle about X the joint bends by for the end ({@code foot} from the joint, {@code knee}
     * from the pivot) to be {@code sqrt(distSq)} from the pivot: of the two, the smaller one that
     * doesn't bend it backwards. Out of reach, as near as it gets.
     */
    static double bendFor(double[] knee, double[] foot, float bend, double distSq)
    {
        // |knee + rotX(k)·foot|² = |knee|² + |foot|² + 2·(kx·fx + A·cos k + B·sin k)
        final double a = knee[1] * foot[1] + knee[2] * foot[2];
        final double b = knee[2] * foot[1] - knee[1] * foot[2];
        final double r = Math.sqrt(a * a + b * b);
        if (r <= 1e-6)
        {
            return 0;
        }
        final double k = (distSq - dot(knee, knee) - dot(foot, foot) - 2 * knee[0] * foot[0]) / 2;
        final double phase = Math.atan2(b, a);
        final double spread = Math.acos(clamp(k / r));
        return LegIk.pickBend(LegIk.wrapPi(phase + spread), LegIk.wrapPi(phase - spread), bend);
    }

    /**
     * Which way a joint bending by {@code bend} about X sticks out of the line from its segment's
     * pivot to {@code end}: across that line and the hinge. A joint bent by a positive angle (a
     * knee) brings the end round to +Z under it, so it sticks out to -Z.
     */
    static double[] protrusion(double[] end, float bend)
    {
        return new double[] {0, bend * end[2], -bend * end[1]};
    }

    /**
     * The rotation that turns {@code end} (from the pivot) onto {@code target}, and the joint's
     * {@code protrusion} towards {@code direction} (or {@code fallback}) about that line.
     */
    private static void aim(double[] end, double[] protrusion, double[] target, double[] direction, double[] fallback, double[] dest)
    {
        final double[] from = normalized(end);
        if (from == null)
        {
            identity(dest);
            return;
        }
        double[] fromSide = across(protrusion, from);
        if (fromSide == null)
        {
            fromSide = perpendicular(from);
        }
        double[] to = normalized(target);
        if (to == null)
        {
            to = from;
        }
        double[] toSide = across(direction, to);
        if (toSide == null)
        {
            toSide = across(fallback, to);
        }
        if (toSide == null)
        {
            toSide = perpendicular(to);
        }
        fromFrames(from, fromSide, to, toSide, dest);
    }

    /** A point {@code fromPivot} from the pivot and {@code fromTarget} from the target, sticking out towards {@code direction}. */
    private static double[] place(double[] target, double fromPivot, double fromTarget, double[] direction, double[] fallback)
    {
        double[] along = normalized(target);
        double distance = length(target);
        double[] side;
        if (along == null)
        {
            along = normalized(direction != null ? direction : fallback);
            if (along == null)
            {
                along = new double[] {0, 1, 0};
            }
            return scaled(along, fromPivot);
        }
        side = across(direction, along);
        if (side == null)
        {
            side = across(fallback, along);
        }
        if (side == null)
        {
            side = perpendicular(along);
        }
        if (fromPivot < EPSILON)
        {
            return new double[3];
        }
        final double cos = clamp((fromPivot * fromPivot + distance * distance - fromTarget * fromTarget) / (2 * fromPivot * distance));
        final double sin = Math.sqrt(1 - cos * cos);
        return add(scaled(along, fromPivot * cos), scaled(side, fromPivot * sin));
    }

    /** {@code v} without its part along the unit {@code axis}, normalised; null if nothing is left. */
    private static double[] across(double[] v, double[] axis)
    {
        if (v == null)
        {
            return null;
        }
        final double length = length(v);
        if (length < EPSILON)
        {
            return null;
        }
        final double[] rest = sub(v, scaled(axis, dot(v, axis)));
        final double restLength = length(rest);
        return restLength < 1e-4 * length ? null : scaled(rest, 1 / restLength);
    }

    /** Any unit vector across the unit {@code v}. */
    private static double[] perpendicular(double[] v)
    {
        final double[] other = Math.abs(v[0]) < 0.9 ? new double[] {1, 0, 0} : new double[] {0, 1, 0};
        return normalized(cross(v, other));
    }

    /** The rotation turning the orthonormal pair (a1, a2) onto (b1, b2). */
    private static void fromFrames(double[] a1, double[] a2, double[] b1, double[] b2, double[] dest)
    {
        final double[] a3 = cross(a1, a2);
        final double[] b3 = cross(b1, b2);
        final double[][] m = new double[3][3];
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                m[i][j] = b1[i] * a1[j] + b2[i] * a2[j] + b3[i] * a3[j];
            }
        }
        final double trace = m[0][0] + m[1][1] + m[2][2];
        if (trace > 0)
        {
            final double s = Math.sqrt(trace + 1) * 2;
            set(dest, (m[2][1] - m[1][2]) / s, (m[0][2] - m[2][0]) / s, (m[1][0] - m[0][1]) / s, 0.25 * s);
        }
        else if (m[0][0] > m[1][1] && m[0][0] > m[2][2])
        {
            final double s = Math.sqrt(1 + m[0][0] - m[1][1] - m[2][2]) * 2;
            set(dest, 0.25 * s, (m[0][1] + m[1][0]) / s, (m[0][2] + m[2][0]) / s, (m[2][1] - m[1][2]) / s);
        }
        else if (m[1][1] > m[2][2])
        {
            final double s = Math.sqrt(1 + m[1][1] - m[0][0] - m[2][2]) * 2;
            set(dest, (m[0][1] + m[1][0]) / s, 0.25 * s, (m[1][2] + m[2][1]) / s, (m[0][2] - m[2][0]) / s);
        }
        else
        {
            final double s = Math.sqrt(1 + m[2][2] - m[0][0] - m[1][1]) * 2;
            set(dest, (m[0][2] + m[2][0]) / s, (m[1][2] + m[2][1]) / s, 0.25 * s, (m[1][0] - m[0][1]) / s);
        }
        normalize(dest);
    }

    /** The shortest rotation turning {@code from} onto {@code to}; false (dest untouched) where there is none to make. */
    private static boolean shortestArc(double[] from, double[] to, double[] dest)
    {
        final double[] a = normalized(from), b = normalized(to);
        if (a == null || b == null)
        {
            return false;
        }
        final double dot = dot(a, b);
        if (dot > 1 - 1e-12 || dot < -0.999999)
        {
            return false;
        }
        final double[] axis = cross(a, b);
        set(dest, axis[0], axis[1], axis[2], 1 + dot);
        normalize(dest);
        return true;
    }

    // Quaternions, {x, y, z, w}.

    static void identity(double[] dest)
    {
        set(dest, 0, 0, 0, 1);
    }

    static void axisAngleX(double angle, double[] dest)
    {
        set(dest, Math.sin(angle / 2), 0, 0, Math.cos(angle / 2));
    }

    /** About the unit {@code axis}. */
    static void axisAngle(double[] axis, double angle, double[] dest)
    {
        final double s = Math.sin(angle / 2);
        set(dest, axis[0] * s, axis[1] * s, axis[2] * s, Math.cos(angle / 2));
    }

    /** {@code dest = left · right}; dest may be either. */
    static void multiply(double[] left, double[] right, double[] dest)
    {
        final double x = left[0] * right[3] + left[3] * right[0] + left[1] * right[2] - left[2] * right[1];
        final double y = left[1] * right[3] + left[3] * right[1] + left[2] * right[0] - left[0] * right[2];
        final double z = left[2] * right[3] + left[3] * right[2] + left[0] * right[1] - left[1] * right[0];
        final double w = left[3] * right[3] - left[0] * right[0] - left[1] * right[1] - left[2] * right[2];
        set(dest, x, y, z, w);
    }

    static double[] rotate(double[] q, double[] v)
    {
        // v + w·t + q×t, with t = 2·(q×v).
        final double tx = 2 * (q[1] * v[2] - q[2] * v[1]);
        final double ty = 2 * (q[2] * v[0] - q[0] * v[2]);
        final double tz = 2 * (q[0] * v[1] - q[1] * v[0]);
        return new double[] {
                v[0] + q[3] * tx + q[1] * tz - q[2] * ty,
                v[1] + q[3] * ty + q[2] * tx - q[0] * tz,
                v[2] + q[3] * tz + q[0] * ty - q[1] * tx};
    }

    static double[] rotateInverse(double[] q, double[] v)
    {
        return rotate(new double[] {-q[0], -q[1], -q[2], q[3]}, v);
    }

    private static void normalize(double[] q)
    {
        final double length = Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
        for (int i = 0; i < 4; i++)
        {
            q[i] /= length;
        }
    }

    private static void set(double[] dest, double x, double y, double z, double w)
    {
        dest[0] = x;
        dest[1] = y;
        dest[2] = z;
        dest[3] = w;
    }

    // Vectors.

    private static double[] rotateX(double[] v, double angle)
    {
        final double cos = Math.cos(angle), sin = Math.sin(angle);
        return new double[] {v[0], v[1] * cos - v[2] * sin, v[1] * sin + v[2] * cos};
    }

    private static double[] add(double[] a, double[] b)
    {
        return new double[] {a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    private static double[] sub(double[] a, double[] b)
    {
        return new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    private static double[] scaled(double[] v, double s)
    {
        return new double[] {v[0] * s, v[1] * s, v[2] * s};
    }

    private static void scale(double[] v, double s)
    {
        v[0] *= s;
        v[1] *= s;
        v[2] *= s;
    }

    private static double dot(double[] a, double[] b)
    {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private static double[] cross(double[] a, double[] b)
    {
        return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    private static double length(double[] v)
    {
        return Math.sqrt(dot(v, v));
    }

    /** {@code v} of unit length, or null if it has none. */
    private static double[] normalized(double[] v)
    {
        if (v == null)
        {
            return null;
        }
        final double length = length(v);
        return length < EPSILON ? null : scaled(v, 1 / length);
    }

    private static double clamp(double v)
    {
        return Math.max(-1, Math.min(1, v));
    }

}
