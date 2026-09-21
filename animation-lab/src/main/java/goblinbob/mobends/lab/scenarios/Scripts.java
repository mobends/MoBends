package goblinbob.mobends.lab.scenarios;

import goblinbob.mobends.lab.sim.EntityInputs;

/** Small reusable input building blocks for scenarios. */
public class Scripts
{
    public static final double WALK_SPEED = 0.15D;
    public static final double SPRINT_SPEED = 0.26D;
    public static final double SNEAK_SPEED = 0.06D;

    public static void walk(EntityInputs in, double speed)
    {
        in.forwardSpeed = speed;
    }

    /** A slow head sweep: yaw and pitch as sine waves of the tick. */
    public static void lookAround(EntityInputs in, int tick)
    {
        in.headYaw = (float) (Math.sin(tick * 0.08) * 45.0);
        in.headPitch = (float) (Math.cos(tick * 0.05) * 25.0);
    }

    public static boolean between(int tick, int from, int toExclusive)
    {
        return tick >= from && tick < toExclusive;
    }
}
