package goblinbob.mobends.lab.sim;

import goblinbob.mobends.core.client.event.DataUpdateHandler;

/**
 * Drives the mod's global timing state ({@link DataUpdateHandler}) the way the client render loop
 * does: a fixed frame rate, ticks advancing at 20 per second, and partial ticks in between.
 *
 * The game computes ticksPerFrame as the (clamped) difference of "ticksExisted + renderTickTime"
 * between two frames. Here that is simply 20 / fps, which is what a steady frame rate produces.
 */
public class LabClock
{
    private final int fps;
    private int frame = -1;
    private int lastTick = -1;

    public LabClock(int fps)
    {
        this.fps = fps;
        DataUpdateHandler.reset();
    }

    public int getFps()
    {
        return fps;
    }

    public float getTicksPerFrame()
    {
        return 20.0F / fps;
    }

    /**
     * Advances to the next frame.
     *
     * @return the number of whole ticks that started since the previous frame (0 or 1 at any
     *         frame rate of 20 fps or more).
     */
    public int nextFrame()
    {
        frame++;
        // Kept as an exact rational to avoid drift: ticks = frame * 20 / fps.
        double ticks = frame * 20.0D / fps;
        int tick = (int) Math.floor(ticks + 1e-9);
        float partial = (float) (ticks - tick);
        if (partial < 0) partial = 0;

        float previous = DataUpdateHandler.getTicks();
        float now = (float) ticks;
        DataUpdateHandler.partialTicks = partial;
        DataUpdateHandler.ticksPerFrame = frame == 0 ? 0.0F : Math.min(Math.max(0.0F, now - previous), 1.0F);
        DataUpdateHandler.setTicks(now);

        int ticksStarted = tick - lastTick;
        lastTick = tick;
        return ticksStarted;
    }

    public int getFrame()
    {
        return frame;
    }

    public int getTick()
    {
        return lastTick;
    }

    public float getTicks()
    {
        return DataUpdateHandler.getTicks();
    }

    public float getPartialTicks()
    {
        return DataUpdateHandler.partialTicks;
    }
}
