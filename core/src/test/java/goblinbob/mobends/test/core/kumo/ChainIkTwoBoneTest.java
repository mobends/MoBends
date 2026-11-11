package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.driver.ChainIk;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * {@link ChainIk}'s chains of two, checked by posing them: what they say they miss their target by
 * is how far the posed end really is from it, too far or too near.
 */
public class ChainIkTwoBoneTest
{

    private static final double TOLERANCE = 1e-6;

    @Test
    public void saysHowFarItMissesATargetTooNearToReach()
    {
        // Folded all the way, the end is still 6 from the pivot: it lands at [0, 6, 0], 5 past the target.
        final double[][] chain = {{0, 8, 0}, {0, 2, 0}};
        final double[] target = {0, 1, 0};
        final double[][] rotations = new double[2][4];
        final double stretch = solve(chain, new float[] {0, 1}, target, rotations);
        final double[] end = end(chain, rotations);
        assertEquals(5, distance(end, target), TOLERANCE);
        assertEquals(5, stretch, TOLERANCE);
    }

    @Test
    public void saysHowFarItMissesAnyTarget()
    {
        final double[][][] chains = {
                {{0, 8, 0}, {0, 2, 0}},
                {{0, 5, 0}, {0, 5, 0}},
                {{0, 12.5, 3}, {-11, 15, -3}},
        };
        final double[][] directions = {{0, 1, 0}, {1, 0, 0}, {0, 0, -1}, {1, -1, 1}, {-0.3, 2, 0.7}};
        final double[] distances = {0, 0.5, 1, 3, 6, 9, 10, 15, 30, 50};
        for (double[][] chain : chains)
        {
            for (float bend : new float[] {1, -1})
            {
                for (double[] direction : directions)
                {
                    final double directionLength = length(direction);
                    for (double distance : distances)
                    {
                        final double[] target = {
                                direction[0] * distance / directionLength,
                                direction[1] * distance / directionLength,
                                direction[2] * distance / directionLength};
                        final double[][] rotations = new double[2][4];
                        final double stretch = solve(chain, new float[] {0, bend}, target, rotations);
                        assertEquals(distance(end(chain, rotations), target), stretch, TOLERANCE);
                    }
                }
            }
        }
    }

    private static double solve(double[][] chain, float[] bends, double[] target, double[][] rotations)
    {
        return ChainIk.solve(chain, bends, new double[][] {null, {0, 0, 1}}, new double[][] {null, {0, 0, 1}}, target, rotations);
    }

    /** Where the posed chain's end is, from its first pivot. */
    static double[] end(double[][] segments, double[][] rotations)
    {
        double[] frame = {0, 0, 0, 1};
        double[] at = {0, 0, 0};
        for (int i = 0; i < segments.length; i++)
        {
            frame = multiply(frame, rotations[i]);
            final double[] step = rotate(frame, segments[i]);
            at = new double[] {at[0] + step[0], at[1] + step[1], at[2] + step[2]};
        }
        return at;
    }

    static double[] multiply(double[] l, double[] r)
    {
        return new double[] {
                l[0] * r[3] + l[3] * r[0] + l[1] * r[2] - l[2] * r[1],
                l[1] * r[3] + l[3] * r[1] + l[2] * r[0] - l[0] * r[2],
                l[2] * r[3] + l[3] * r[2] + l[0] * r[1] - l[1] * r[0],
                l[3] * r[3] - l[0] * r[0] - l[1] * r[1] - l[2] * r[2]};
    }

    static double[] rotate(double[] q, double[] v)
    {
        final double tx = 2 * (q[1] * v[2] - q[2] * v[1]);
        final double ty = 2 * (q[2] * v[0] - q[0] * v[2]);
        final double tz = 2 * (q[0] * v[1] - q[1] * v[0]);
        return new double[] {
                v[0] + q[3] * tx + q[1] * tz - q[2] * ty,
                v[1] + q[3] * ty + q[2] * tx - q[0] * tz,
                v[2] + q[3] * tz + q[0] * ty - q[1] * tx};
    }

    static double distance(double[] a, double[] b)
    {
        return length(new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]});
    }

    static double length(double[] v)
    {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

}
