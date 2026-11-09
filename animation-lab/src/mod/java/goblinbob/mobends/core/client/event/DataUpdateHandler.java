package goblinbob.mobends.core.client.event;

/**
 * LAB SHIM. Replaces the mod's clock, driven by the game's ticks and frames. Holds exactly the same
 * static state the animation code reads (partialTicks, ticks, ticksPerFrame, frame); the lab clock
 * drives it.
 */
public final class DataUpdateHandler {
    public static float partialTicks = 0.0f;
    private static float ticks = 0.0f;
    public static float ticksPerFrame = 0.0f;
    /** Counts the frames; the lab clock advances it as each one begins. */
    public static int frame = 0;

    private DataUpdateHandler() {
    }

    public static float getTicks() {
        return ticks;
    }

    /** Lab only: the clock advances the global tick counter. */
    public static void setTicks(float value) {
        ticks = value;
    }

    /** Lab only: resets all global timing state between scenarios. */
    public static void reset() {
        partialTicks = 0.0f;
        ticks = 0.0f;
        ticksPerFrame = 0.0f;
        frame = 0;
    }
}
