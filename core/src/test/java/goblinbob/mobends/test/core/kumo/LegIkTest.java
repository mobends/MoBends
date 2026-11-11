package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.driver.LegIk;
import goblinbob.mobends.core.math.Quaternion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** The two-segment leg IK: the upper and lower rotations put the sole on the target. */
public class LegIkTest
{

    /** {@code v} rotated by {@code q} (q v q*). */
    private static double[] rotate(Quaternion q, double[] v)
    {
        double x = q.x, y = q.y, z = q.z, w = q.w;
        // t = 2 (q.xyz × v); v' = v + w t + q.xyz × t
        double tx = 2 * (y * v[2] - z * v[1]);
        double ty = 2 * (z * v[0] - x * v[2]);
        double tz = 2 * (x * v[1] - y * v[0]);
        return new double[] {
                v[0] + w * tx + (y * tz - z * ty),
                v[1] + w * ty + (z * tx - x * tz),
                v[2] + w * tz + (x * ty - y * tx)
        };
    }

    /** Where the sole ends up, relative to the upper segment's pivot, once the leg is solved for {@code target}. */
    private static double[] solvedSole(float[] knee, float[] foot, double twist, double[] target)
    {
        Quaternion upper = new Quaternion(), lower = new Quaternion();
        LegIk.solve(knee, foot, 1, twist, target[0], target[1], target[2], upper, lower);
        double[] sole = rotate(lower, new double[] { foot[0], foot[1], foot[2] });
        return rotate(upper, new double[] { knee[0] + sole[0], knee[1] + sole[1], knee[2] + sole[2] });
    }

    private static void assertReaches(float[] knee, float[] foot, double twist, double[] target)
    {
        double[] sole = solvedSole(knee, foot, twist, target);
        String message = String.format("knee [%s, %s, %s], twist %s: sole at [%.3f, %.3f, %.3f], target [%s, %s, %s]", knee[0], knee[1], knee[2], twist,
                sole[0], sole[1], sole[2], target[0], target[1], target[2]);
        for (int i = 0; i < 3; i++)
        {
            assertEquals(message, target[i], sole[i], 1e-4);
        }
    }

    @Test
    public void aStraightLegTurnedToTheOppositeWayReachesItsTarget()
    {
        // A straight leg whose target is straight behind where it points, whatever that direction is:
        // half a turn about an axis across the leg, not always about X.
        float[][] directions = {
                { 1, 1, 0 },  // angled in the XY plane: half a turn about X put the foot at [2, -2, 0]
                { 0, 1, 0 },  // straight down
                { 0, 0, 1 },  // along Z
                { 1, 0, 0 },  // along X, across which X turns nothing
                { -1, 0, 0 },
                { 1, 2, 3 },
                { 0.3F, -1, 0.5F },
        };
        for (float[] d : directions)
        {
            double[] target = { -2 * d[0], -2 * d[1], -2 * d[2] };
            assertReaches(d, d, 0, target);
            // Twisted first, the swing turns from wherever the twist left it.
            float[] knee = { d[0], d[1], d[2] };
            double[] turned = { -2 * (d[0] * Math.cos(0.7) + d[2] * Math.sin(0.7)), -2 * d[1], -2 * (-d[0] * Math.sin(0.7) + d[2] * Math.cos(0.7)) };
            assertReaches(knee, d, 0.7, turned);
        }
    }

    @Test
    public void aLegHangingDownTurnedUpTurnsAboutX()
    {
        // The knee's own axis, as before: the leg in the YZ plane keeps X as its turn.
        Quaternion upper = new Quaternion(), lower = new Quaternion();
        LegIk.solve(new float[] { 0, 6, 0 }, new float[] { 0, 6, 0 }, 1, 0, 0, -12, 0, upper, lower);
        assertEquals(1, Math.abs(upper.x), 1e-5);
        assertEquals(0, upper.w, 1e-5);
    }

    @Test
    public void aBentLegReachesATargetWithinReach()
    {
        assertReaches(new float[] { 0, 6, 0 }, new float[] { 0, 6, 0 }, 0, new double[] { 0, 8, -3 });
        assertReaches(new float[] { 0, 6, 0 }, new float[] { 0, 6, 0 }, 0.4, new double[] { 2, 9, 1 });
    }

}
