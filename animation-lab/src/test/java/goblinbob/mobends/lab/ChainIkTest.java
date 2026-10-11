package goblinbob.mobends.lab;

import goblinbob.mobends.core.kumo.driver.ChainIk;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link ChainIk}: chains of hinged segments put on a target, each joint pointing its way. */
class ChainIkTest
{

    /** The golem's right arm: the shoulder at the body's middle, the elbow hinged at the back, the hand off to the side. */
    private static final double[][] ARM = {{0, 12.5, 3}, {-11, 15, -3}};
    /** A leg of three segments, the foot a little to the side. */
    private static final double[][] LEG = {{0, 8, 0}, {0, 7, 0}, {-2, 6, 0}};

    private static double[][] solve(double[][] segments, float[] bends, double[][] directions, double[] target, double[] shortfall)
    {
        double[][] rotations = new double[segments.length][4];
        double[][] fallbacks = new double[segments.length][];
        for (int i = 1; i < segments.length; i++)
        {
            fallbacks[i] = new double[] {0, 0, 1};
        }
        shortfall[0] = ChainIk.solve(segments, bends, directions, fallbacks, target, rotations);
        return rotations;
    }

    /** The pivots after the first, then the end, from the first pivot. */
    private static double[][] points(double[][] segments, double[][] rotations)
    {
        double[][] points = new double[segments.length][];
        double[] frame = {0, 0, 0, 1};
        double[] at = {0, 0, 0};
        for (int i = 0; i < segments.length; i++)
        {
            multiply(frame, rotations[i], frame);
            double[] step = rotate(frame, segments[i]);
            at = new double[] {at[0] + step[0], at[1] + step[1], at[2] + step[2]};
            points[i] = at;
        }
        return points;
    }

    /** {@code point}'s offset from the line through the origin and {@code along}. */
    private static double[] offLine(double[] point, double[] along)
    {
        double length = Math.sqrt(dot(along, along));
        double s = dot(point, along) / (length * length);
        return new double[] {point[0] - along[0] * s, point[1] - along[1] * s, point[2] - along[2] * s};
    }

    private static void assertHinge(double[] q, float bend)
    {
        assertEquals(0, q[1], 1e-4, "turns only about X");
        assertEquals(0, q[2], 1e-4, "turns only about X");
        assertTrue(2 * Math.atan2(q[0], q[3]) * bend >= -1e-6, "never bends backwards");
    }

    @Test
    void twoSegmentsReachTheirTargetWithTheJointWhereItPoints()
    {
        double[][] targets = {{-10, 20, -5}, {-14, 6, -16}, {0, 25, 4}, {-20, 0, -10}};
        double[][] directions = {{0, 0, 1}, {1, 0, 1}, {0, -1, 0.5}};
        double[] shortfall = new double[1];
        for (double[] target : targets)
        {
            for (double[] direction : directions)
            {
                double[][] rotations = solve(ARM, new float[] {0, -1}, new double[][] {null, direction}, target, shortfall);
                double[] hand = points(ARM, rotations)[1];
                for (int i = 0; i < 3; i++)
                {
                    assertEquals(target[i], hand[i], 1e-6);
                }
                assertEquals(0, shortfall[0], 1e-9);
                assertHinge(rotations[1], -1);
                // The elbow (its hinge's bend side, the hand being off to the side) sticks out
                // towards where it points.
                double[] out = rotate(rotations[0], new double[] {0, -ARM[0][2], ARM[0][1]});
                assertTrue(dot(offLine(out, target), direction) > 0, "the elbow points its way");
            }
        }
    }

    @Test
    void twoSegmentsOutOfReachPointAtTheTarget()
    {
        double[] target = {0, 80, -40};
        double[] shortfall = new double[1];
        double[][] rotations = solve(ARM, new float[] {0, -1}, new double[][] {null, {0, 0, 1}}, target, shortfall);
        double[] hand = points(ARM, rotations)[1];
        double length = Math.sqrt(dot(hand, hand));
        assertEquals(Math.sqrt(dot(target, target)) - length, shortfall[0], 1e-6);
        double[] off = offLine(hand, target);
        assertEquals(0, Math.sqrt(dot(off, off)), 1e-6, "pointing at the target");
    }

    @Test
    void threeSegmentsBendInOnePlaneWhenTheirJointsAgree()
    {
        double[] shortfall = new double[1];
        double[][] targets = {{-2, 15, -3}, {3, 12, -6}, {-1, 14, -5}};
        // Bending the same way, both pointing forward: a C. A knee then an ankle bending the other
        // way, pointing opposite ways: a Z (a hind leg).
        float[][] bends = {{0, 1, 1}, {0, 1, -1}};
        double[][][] directions = {{null, {0, 0, -1}, {0, 0, -1}}, {null, {0, 0, -1}, {0, 0, 1}}};
        for (int shape = 0; shape < 2; shape++)
        {
            for (double[] target : targets)
            {
                double[][] rotations = solve(LEG, bends[shape], directions[shape], target, shortfall);
                double[] foot = points(LEG, rotations)[2];
                for (int i = 0; i < 3; i++)
                {
                    assertEquals(target[i], foot[i], 1e-4);
                }
                assertHinge(rotations[1], bends[shape][1]);
                assertHinge(rotations[2], bends[shape][2]);
            }
        }
    }

    @Test
    void threeSegmentsPointEachJointItsOwnWay()
    {
        double[] target = {0, 14, 0};
        double[] first = {1, 0, 0}, second = {0, 0, 1};
        double[] shortfall = new double[1];
        double[][] segments = {{0, 8, 0}, {0, 7, 0}, {0, 6, 0}};
        double[][] rotations = solve(segments, new float[] {0, 1, 1}, new double[][] {null, first, second}, target, shortfall);
        double[][] points = points(segments, rotations);
        for (int i = 0; i < 3; i++)
        {
            assertEquals(target[i], points[2][i], 1e-4);
        }
        // The second joint sticks out from the line to the target its way, the first from the line to the second joint.
        assertTrue(dot(offLine(points[1], target), second) > 1, "the second joint points its way");
        assertTrue(dot(offLine(points[0], points[1]), first) > 1, "the first joint points its way");
        assertHinge(rotations[2], 1);
    }

    // Quaternions {x, y, z, w}, as ChainIk has them.

    private static void multiply(double[] a, double[] b, double[] dest)
    {
        double x = a[0] * b[3] + a[3] * b[0] + a[1] * b[2] - a[2] * b[1];
        double y = a[1] * b[3] + a[3] * b[1] + a[2] * b[0] - a[0] * b[2];
        double z = a[2] * b[3] + a[3] * b[2] + a[0] * b[1] - a[1] * b[0];
        double w = a[3] * b[3] - a[0] * b[0] - a[1] * b[1] - a[2] * b[2];
        dest[0] = x; dest[1] = y; dest[2] = z; dest[3] = w;
    }

    private static double[] rotate(double[] q, double[] v)
    {
        double[] p = {v[0], v[1], v[2], 0};
        double[] r = new double[4];
        multiply(q, p, r);
        multiply(r, new double[] {-q[0], -q[1], -q[2], q[3]}, r);
        return new double[] {r[0], r[1], r[2]};
    }

    private static double dot(double[] a, double[] b)
    {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

}
