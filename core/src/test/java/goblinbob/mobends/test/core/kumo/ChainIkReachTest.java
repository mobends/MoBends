package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.driver.ChainIk;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link ChainIk}'s chains of three, checked by posing them: every target they can reach, near the
 * first pivot too, is reached, and one they can't is missed by as little as they can, which is what
 * they say they fall short by.
 */
public class ChainIkReachTest
{

    private static final double TOLERANCE = 1e-4;

    /** Straight segments, so each chain reaches from {@code max(0, longest - the others)} to all three together. */
    private static final double[][][] CHAINS = {
            {{0, 6, 0}, {0, 6, 0}, {0, 6, 0}},
            {{0, 4, 0}, {0, 4, 0}, {0, 10, 0}},
            {{0, 10, 0}, {0, 3, 0}, {0, 4, 0}},
            {{0, 8, 0}, {0, 7, 0}, {0, 6, 0}},
    };

    /** A C (both bending forward), a Z (a hind leg), and the joints pointing out of one plane. */
    private static final float[][] BENDS = {{0, 1, 1}, {0, 1, -1}, {0, 1, 1}};
    private static final double[][][] DIRECTIONS = {
            {null, {0, 0, 1}, {0, 0, 1}},
            {null, {0, 0, -1}, {0, 0, 1}},
            {null, {1, 0, 0}, {0, 0, 1}},
    };

    private static final double[][] TARGET_DIRECTIONS = {
            {0, 1, 0}, {0, -1, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1},
            {1, 1, 1}, {-1, 1, -1}, {1, -1, -1}, {-1, -1, 1},
            {0.3, 2, -0.7}, {-0.2, 0.5, 1.9}, {1.4, -0.3, 0.2},
    };

    @Test
    public void reachesATargetNearItsRoot()
    {
        // Found by posing the solved chain: the end landed at about [0, 6.89, 0], said to reach.
        final double[][] chain = CHAINS[0];
        final double[] target = {0, 2, 0};
        final double[][] rotations = new double[3][4];
        final double shortfall = solve(chain, BENDS[0], DIRECTIONS[0], target, rotations);
        assertEquals(0, distance(end(chain, rotations), target), TOLERANCE);
        assertEquals(0, shortfall, TOLERANCE);
    }

    @Test
    public void reachesEveryTargetInReachAndSaysHowFarItMissesTheOthersBy()
    {
        final List<String> failures = new ArrayList<>();
        for (int c = 0; c < CHAINS.length; c++)
        {
            final double[][] chain = CHAINS[c];
            final double l0 = length(chain[0]), l1 = length(chain[1]), l2 = length(chain[2]);
            final double total = l0 + l1 + l2;
            final double nearest = Math.max(0, Math.max(l0, Math.max(l1, l2)) * 2 - total);
            final double[] distances = {
                    0.1, 0.5, 1, 2, 3, nearest - 1, nearest - 0.1, nearest + 0.1, nearest + 1,
                    total * 0.25, total * 0.5, total * 0.75, total * 0.9, total - 0.1, total - 0.01,
                    total + 0.5, total + 5,
            };
            for (int shape = 0; shape < BENDS.length; shape++)
            {
                for (double[] direction : TARGET_DIRECTIONS)
                {
                    final double directionLength = length(direction);
                    for (double distance : distances)
                    {
                        if (distance <= 0)
                        {
                            continue;
                        }
                        final double[] target = {
                                direction[0] * distance / directionLength,
                                direction[1] * distance / directionLength,
                                direction[2] * distance / directionLength};
                        final double[][] rotations = new double[3][4];
                        final double shortfall = solve(chain, BENDS[shape], DIRECTIONS[shape], target, rotations);
                        final double missed = distance(end(chain, rotations), target);
                        final double expected = Math.max(0, Math.max(nearest - distance, distance - total));
                        final String where = String.format("chain %d, shape %d, target [%.3f, %.3f, %.3f]",
                                                           c, shape, target[0], target[1], target[2]);
                        if (Math.abs(missed - expected) > TOLERANCE)
                        {
                            failures.add(String.format("%s: misses by %.4f, could by %.4f", where, missed, expected));
                        }
                        if (Math.abs(missed - shortfall) > 1e-9)
                        {
                            failures.add(String.format("%s: misses by %.4f, says %.4f", where, missed, shortfall));
                        }
                        if (!isHinge(rotations[2], BENDS[shape][2]))
                        {
                            failures.add(where + ": the last joint isn't a hinge bending its way");
                        }
                    }
                }
            }
        }
        assertTrue(failures.size() + " failures, the first:\n" + String.join("\n", failures.subList(0, Math.min(20, failures.size()))),
                   failures.isEmpty());
    }

    private static double solve(double[][] segments, float[] bends, double[][] directions, double[] target, double[][] rotations)
    {
        final double[][] fallbacks = new double[segments.length][];
        for (int i = 1; i < segments.length; i++)
        {
            fallbacks[i] = new double[] {0, 0, 1};
        }
        return ChainIk.solve(segments, bends, directions, fallbacks, target, rotations);
    }

    /** The last joint is a hinge about X, never bent backwards (the middle segment also twists, so isn't one). */
    private static boolean isHinge(double[] q, float bend)
    {
        return Math.abs(q[1]) < 1e-9 && Math.abs(q[2]) < 1e-9 && wrap(2 * Math.atan2(q[0], q[3])) * bend >= -1e-6;
    }

    /** Forward kinematics: where the end is, from the first pivot. */
    private static double[] end(double[][] segments, double[][] rotations)
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

    private static double[] multiply(double[] a, double[] b)
    {
        return new double[] {
                a[0] * b[3] + a[3] * b[0] + a[1] * b[2] - a[2] * b[1],
                a[1] * b[3] + a[3] * b[1] + a[2] * b[0] - a[0] * b[2],
                a[2] * b[3] + a[3] * b[2] + a[0] * b[1] - a[1] * b[0],
                a[3] * b[3] - a[0] * b[0] - a[1] * b[1] - a[2] * b[2]};
    }

    private static double[] rotate(double[] q, double[] v)
    {
        final double[] r = multiply(multiply(q, new double[] {v[0], v[1], v[2], 0}), new double[] {-q[0], -q[1], -q[2], q[3]});
        return new double[] {r[0], r[1], r[2]};
    }

    private static double wrap(double angle)
    {
        return Math.atan2(Math.sin(angle), Math.cos(angle));
    }

    private static double distance(double[] a, double[] b)
    {
        return length(new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]});
    }

    private static double length(double[] v)
    {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

}
