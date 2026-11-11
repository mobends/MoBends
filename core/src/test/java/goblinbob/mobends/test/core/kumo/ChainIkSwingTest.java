package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.driver.ChainIk;
import org.junit.Test;

import static goblinbob.mobends.test.core.kumo.ChainIkTwoBoneTest.distance;
import static goblinbob.mobends.test.core.kumo.ChainIkTwoBoneTest.end;
import static goblinbob.mobends.test.core.kumo.ChainIkTwoBoneTest.length;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link ChainIk}'s chains of three swing their end onto the line to the target last, checked by
 * posing them: an end facing straight away from the target is swung round too.
 */
public class ChainIkSwingTest
{

    private static final double TOLERANCE = 1e-6;

    private static final double[][] CHAIN = {{0, 10, 0}, {0, 2, 0}, {0, 2, 0}};
    private static final float[] BENDS = {0, 1, 1};
    private static final double[][] DIRECTIONS = {null, {0, 0, 1}, {0, 0, 1}};

    @Test
    public void swingsAnEndFacingAwayFromTheTargetRound()
    {
        // The chain can't fold nearer than 6, and solved for a target 1 away it ended up at about
        // [0, -6, 0], 7 from it, where half a turn takes it to [0, 6, 0], 5 from it.
        final double[] target = {0, 1, 0};
        final double[][] rotations = new double[3][4];
        final double stretch = ChainIk.solve(CHAIN, BENDS, DIRECTIONS, DIRECTIONS, target, rotations);
        final double[] end = end(CHAIN, rotations);
        assertOnTheLineToTheTarget(end, target);
        assertTrue("the end is at " + format(end), end[1] > 0);
        assertEquals(length(end) - length(target), distance(end, target), TOLERANCE);
        assertEquals(distance(end, target), stretch, TOLERANCE);
    }

    @Test
    public void endsOnTheLineToEveryTarget()
    {
        final double[][] targetDirections = {
                {0, 1, 0}, {0, -1, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {1, 1, 1}, {-0.3, 2, -0.7},
        };
        final double[] distances = {0.5, 1, 2, 4, 5.5, 6.5, 8, 10, 13, 14, 20};
        for (double[] direction : targetDirections)
        {
            final double directionLength = length(direction);
            for (double distance : distances)
            {
                final double[] target = {
                        direction[0] * distance / directionLength,
                        direction[1] * distance / directionLength,
                        direction[2] * distance / directionLength};
                final double[][] rotations = new double[3][4];
                final double stretch = ChainIk.solve(CHAIN, BENDS, DIRECTIONS, DIRECTIONS, target, rotations);
                final double[] end = end(CHAIN, rotations);
                assertOnTheLineToTheTarget(end, target);
                assertEquals(distance(end, target), stretch, TOLERANCE);
            }
        }
    }

    /** {@code end} lies on the ray from the first pivot through {@code target}. */
    private static void assertOnTheLineToTheTarget(double[] end, double[] target)
    {
        final double cos = (end[0] * target[0] + end[1] * target[1] + end[2] * target[2]) / (length(end) * length(target));
        assertEquals("the end at " + format(end) + " lies towards the target at " + format(target), 1, cos, TOLERANCE);
    }

    private static String format(double[] v)
    {
        return String.format("[%.3f, %.3f, %.3f]", v[0], v[1], v[2]);
    }

}
