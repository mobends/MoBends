package goblinbob.mobends.core.math;

/**
 * A rotation smoothed towards its target: each tick {@code progress} advances by {@code smoothness}
 * and the current value is the normalised blend of {@code start} and {@code end}.
 */
public class SmoothOrientation
{

    protected Quaternion start;
    protected Quaternion end;
    protected Quaternion smooth;
    protected float progress;
    protected float smoothness;

    public SmoothOrientation()
    {
        this.start = new Quaternion();
        this.end = new Quaternion();
        this.smooth = new Quaternion();
        this.progress = 1.0F;
        this.smoothness = 1.0F;
    }

    public Quaternion getEnd()
    {
        return this.end;
    }

    public Quaternion getSmooth()
    {
        return this.smooth;
    }

    public void set(SmoothOrientation other)
    {
        this.start.set(other.start);
        this.end.set(other.end);
        this.smooth.set(other.smooth);
        this.progress = other.progress;
    }

    public SmoothOrientation setSmoothness(float smoothness)
    {
        this.smoothness = smoothness;
        return this;
    }

    public SmoothOrientation set(float x, float y, float z, float w)
    {
        this.start.set(x, y, z, w);
        this.start.normalise();
        this.end.set(this.start);
        this.smooth.set(this.start);
        this.progress = 0F;
        return this;
    }

    public SmoothOrientation add(float x, float y, float z, float w)
    {
        this.start.x += x;
        this.start.y += y;
        this.start.z += z;
        this.start.w += w;
        this.end.set(this.start);
        this.smooth.set(this.start);
        this.progress = 0F;
        return this;
    }

    /** Starts smoothing towards a new target from the current smoothed value. */
    public SmoothOrientation target(Quaternion target)
    {
        this.start.set(this.smooth);
        this.end.set(target);
        this.progress = 0F;
        this.updateSmooth();
        return this;
    }

    /** Jumps straight to the target. */
    public SmoothOrientation snapTo(Quaternion target)
    {
        this.end.set(target);
        this.start.set(this.end);
        this.smooth.set(this.end);
        return this;
    }

    public SmoothOrientation identity()
    {
        this.start.setIdentity();
        this.end.setIdentity();
        this.smooth.setIdentity();
        this.progress = 1.0F;
        return this;
    }

    public SmoothOrientation finish()
    {
        this.smooth.set(this.end);
        this.start.set(this.end);
        this.progress = 1.0F;
        this.updateSmooth();
        return this;
    }

    public void update(float ticksPerFrame)
    {
        this.progress += ticksPerFrame * this.smoothness;
        this.progress = Math.min(this.progress, 1.0F);
        this.updateSmooth();
    }

    public void updateSmooth()
    {
        // q and -q are the same rotation: blend towards whichever is nearer, so the smoothing
        // never takes the long way round (or passes through a near-zero quaternion).
        float dot = start.x * end.x + start.y * end.y + start.z * end.z + start.w * end.w;
        float sign = dot < 0 ? -1F : 1F;
        this.smooth.set(this.start.x + (sign * this.end.x - this.start.x) * this.progress,
                this.start.y + (sign * this.end.y - this.start.y) * this.progress,
                this.start.z + (sign * this.end.z - this.start.z) * this.progress,
                this.start.w + (sign * this.end.w - this.start.w) * this.progress);
        this.smooth.normalise();
    }

}
