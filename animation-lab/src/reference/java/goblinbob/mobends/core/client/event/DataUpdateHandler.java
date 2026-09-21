package goblinbob.mobends.core.client.event;

/**
 * LAB SHIM. Replaces the mod's Forge event handler. Holds exactly the same static state the
 * animation code reads (partialTicks, ticks, ticksPerFrame); the lab clock drives it.
 */
public class DataUpdateHandler
{
    public static float partialTicks = 0.0f;
    protected static float ticks = 0.0f;
    public static float ticksPerFrame = 0.0f;

    public static float getTicks()
    {
        return ticks;
    }

    /** Lab only: the clock advances the global tick counter. */
    public static void setTicks(float value)
    {
        ticks = value;
    }

    /** Lab only: resets all global timing state between scenarios. */
    public static void reset()
    {
        partialTicks = 0.0f;
        ticks = 0.0f;
        ticksPerFrame = 0.0f;
    }
}
