package goblinbob.mobends.standard.kumo.spider;

/**
 * Two-segment leg IK of the spider (each segment 12 units): given the horizontal stretch and the
 * ground level below the hip, the local Z angles of the upper and lower segments.
 */
public final class SpiderLegIk
{

    public static final float SEGMENT_LENGTH = 12F;
    public static final float MAX_STRETCH = SEGMENT_LENGTH * 2;

    private SpiderLegIk()
    {
    }

    /** @return {upperAngle, lowerAngle} in radians, before the odd/even side flip. */
    public static double[] solve(double stretchDistance, double groundLevel)
    {
        double c = groundLevel == 0F ? stretchDistance : Math.sqrt(stretchDistance * stretchDistance + groundLevel * groundLevel);
        if (c > MAX_STRETCH)
        {
            c = MAX_STRETCH;
        }

        final double alpha = c > MAX_STRETCH ? 0 : Math.acos((c / 2) / SEGMENT_LENGTH);
        final double beta = Math.atan2(stretchDistance, -groundLevel);

        double lowerAngle = Math.max(-2.3, -2 * alpha);
        double upperAngle = Math.min(1, alpha + beta - Math.PI / 2);
        return new double[] { upperAngle, lowerAngle };
    }

}
