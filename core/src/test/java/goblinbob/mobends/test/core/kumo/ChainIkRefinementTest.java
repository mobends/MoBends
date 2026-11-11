package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.driver.ChainIk;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link ChainIk}'s chains of three, aimed again and again at a target, end on the closest pose
 * they found, checked by posing them: a target too near is missed by as little as the chain can,
 * and one in reach is still reached.
 */
public class ChainIkRefinementTest
{

    private static final double TOLERANCE = 1e-4;

    @Test
    public void foldsAsFarAsItCanForATargetTooNear()
    {
        // Found by the TypeScript port's review: aimed again, the end drifted out to [0, 7.5, 0]
        // (5 from the target) and said it reached. Folded back, it is 6 from the pivot, 3.5 away.
        final double[][] chain = {{0, 10, 0}, {0, 2, 0}, {0, 2, 0}};
        final double[] target = {0, 2.5, 0};
        final double[][] rotations = new double[3][4];
        final double shortfall = solve(chain, new float[] {0, 1, 1}, new double[][] {null, {0, 0, 1}, {0, 0, 1}}, target, rotations);
        final double[] end = end(chain, rotations);
        assertEquals(0, distance(end, new double[] {0, 6, 0}), TOLERANCE);
        assertEquals(3.5, distance(end, target), TOLERANCE);
        assertEquals(3.5, shortfall, TOLERANCE);
    }

    @Test
    public void missesNoTargetByMoreThanItHasTo()
    {
        // Straight segments of any lengths reach from max(0, longest - the others) to all three together.
        final Random random = new Random(1);
        final List<String> failures = new ArrayList<>();
        for (int run = 0; run < 20000; run++)
        {
            final double[] lengths = {0.5 + random.nextDouble() * 10, 0.5 + random.nextDouble() * 10, 0.5 + random.nextDouble() * 10};
            final double[][] chain = {{0, lengths[0], 0}, {0, lengths[1], 0}, {0, lengths[2], 0}};
            final double total = lengths[0] + lengths[1] + lengths[2];
            final double nearest = Math.max(0, 2 * Math.max(lengths[0], Math.max(lengths[1], lengths[2])) - total);
            final float[] bends = {0, random.nextBoolean() ? 1 : -1, random.nextBoolean() ? 1 : -1};
            final double[][] directions = {null, randomDirection(random), randomDirection(random)};
            final double[] way = {random.nextGaussian(), random.nextGaussian(), random.nextGaussian()};
            final double distance = random.nextDouble() * (total + 2);
            final double scale = distance / length(way);
            final double[] target = {way[0] * scale, way[1] * scale, way[2] * scale};

            final double[][] rotations = new double[3][4];
            final double shortfall = solve(chain, bends, directions, target, rotations);
            final double missed = distance(end(chain, rotations), target);
            final double expected = Math.max(0, Math.max(nearest - distance, distance - total));
            final String where = String.format("lengths [%.3f, %.3f, %.3f], target %.3f away", lengths[0], lengths[1], lengths[2], distance);
            if (Math.abs(missed - expected) > TOLERANCE)
            {
                failures.add(String.format("%s: misses by %.4f, could by %.4f", where, missed, expected));
            }
            if (Math.abs(missed - shortfall) > TOLERANCE)
            {
                failures.add(String.format("%s: misses by %.4f, says %.4f", where, missed, shortfall));
            }
        }
        assertTrue(failures.size() + " failures, the first:\n" + String.join("\n", failures.subList(0, Math.min(20, failures.size()))),
                   failures.isEmpty());
    }

    /** A random direction for a joint, or (one time in four) none. */
    private static double[] randomDirection(Random random)
    {
        return random.nextInt(4) == 0 ? null : new double[] {random.nextGaussian(), random.nextGaussian(), random.nextGaussian()};
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

    private static double distance(double[] a, double[] b)
    {
        return length(new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]});
    }

    private static double length(double[] v)
    {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

}
