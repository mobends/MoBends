package goblinbob.mobends.core.util;

public class Timer
{
    private long time, lastTime;
    private float delta;

    public Timer()
    {
        this.time = System.nanoTime();
        this.lastTime = this.time;
    }

    public float tick()
    {
        this.time = System.nanoTime();
        this.delta = (this.time - this.lastTime) / 1000000F;
        this.lastTime = this.time;

        return this.delta;
    }

    public float getDelta()
    {
        return delta;
    }
}
