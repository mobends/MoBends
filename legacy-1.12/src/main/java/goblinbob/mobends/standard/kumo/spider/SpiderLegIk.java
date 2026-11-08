package goblinbob.mobends.standard.kumo.spider;


/**
 * Two-segment leg IK of the spider: given the horizontal stretch and the ground level below the
 * hip, the local Z angles of the upper and lower segments.
 */
public final class SpiderLegIk
{

    public static final float MAX_STRETCH = SpiderLegs.LEG_SEGMENT_LENGTH * 2;
    /** Where the ground is relative to the hips when the body neither bobs nor kneels, in model units. */
    public static final float REST_GROUND_LEVEL = -7F;

    private SpiderLegIk()
    {
    }

    /** Writes {upperAngle, lowerAngle} in radians, before the odd/even side flip, into {@code angles}. */
    public static void solve(double stretchDistance, double groundLevel, double[] angles)
    {
        double c = groundLevel == 0F ? stretchDistance : Math.sqrt(stretchDistance * stretchDistance + groundLevel * groundLevel);
        c = Math.min(c, MAX_STRETCH);

        final double alpha = Math.acos((c / 2) / SpiderLegs.LEG_SEGMENT_LENGTH);
        final double beta = Math.atan2(stretchDistance, -groundLevel);

        angles[0] = Math.min(1, alpha + beta - Math.PI / 2);
        angles[1] = Math.max(-2.3, -2 * alpha);
    }

}
