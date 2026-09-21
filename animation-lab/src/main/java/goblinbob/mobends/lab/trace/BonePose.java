package goblinbob.mobends.lab.trace;

/**
 * One bone at one frame. Rotations are quaternions (x, y, z, w). "Target" values are the
 * un-smoothed values the animation wrote this frame (the {@code end} of the smoothing filter);
 * they are what a baker samples.
 */
public class BonePose
{
    /** Smoothed rotation, as rendered. */
    public float[] r;
    /** Rotation target. */
    public float[] rt;
    /** Animation offset (IModelPart.getOffset). */
    public float[] o;
    /** Rest position (IModelPart.getPosition). */
    public float[] p;
    /** Per-part global offset (IModelPart.getGlobalOffset). */
    public float[] g;
    /** Scale. */
    public float[] s;

    public BonePose copy()
    {
        BonePose c = new BonePose();
        c.r = r == null ? null : r.clone();
        c.rt = rt == null ? null : rt.clone();
        c.o = o == null ? null : o.clone();
        c.p = p == null ? null : p.clone();
        c.g = g == null ? null : g.clone();
        c.s = s == null ? null : s.clone();
        return c;
    }
}
