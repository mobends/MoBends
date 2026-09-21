package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.bind.IVectorSink;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

/** The per-frame target of one bone slot inside a {@link Pose}. */
public class BoneTarget
{

    /** Absolute rotation (an OVERRIDE write happened). */
    public boolean hasRotation;
    public final Quaternion rotation = new Quaternion();

    /**
     * Relative rotations gathered while no absolute value exists: the result composes as
     * {@code pre * beneath * post}, where "beneath" is the earlier layers' value or the bone's
     * live target. An OVERRIDE write folds them away.
     */
    public boolean hasPre;
    public final Quaternion pre = new Quaternion();
    public boolean hasPost;
    public final Quaternion post = new Quaternion();

    /** Offset / vector written additively (relative to what lies beneath) rather than absolutely. */
    public boolean offsetAdditive;
    public boolean vectorAdditive;

    /** Rotation the bone is snapped to before the target is applied (the "orientInstant then orient" idiom). */
    public boolean hasSnapFrom;
    public final Quaternion snapFrom = new Quaternion();

    /** Vector value the interpolation restarts from (a snap write followed by a retarget in one frame). */
    public boolean hasVectorStart;
    public final Vec3f vectorStart = new Vec3f();

    public boolean hasOffset;
    public final Vec3f offset = new Vec3f();

    public boolean hasVector;
    public final Vec3f vector = new Vec3f();

    /** Damping per tick; NaN keeps whatever the bone had. For vectors per axis. */
    public float smoothness = Float.NaN;
    public final Vec3f vectorSmoothness = new Vec3f(Float.NaN, Float.NaN, Float.NaN);

    /** Jump to the rotation target this frame instead of smoothing. */
    public boolean snap;
    /**
     * Set when a vector write replaced a different target written earlier in the same frame: the
     * original code re-aimed the slide from the current value each time (an exponential approach
     * instead of a linear tween), so the sink restarts the slide on these axes.
     */
    public boolean restartX, restartY, restartZ;
    public IVectorSink.Mode vectorMode = IVectorSink.Mode.RETARGET;

    public void clear()
    {
        hasPre = false;
        hasPost = false;
        offsetAdditive = false;
        vectorAdditive = false;
        hasSnapFrom = false;
        hasVectorStart = false;
        hasRotation = false;
        hasOffset = false;
        hasVector = false;
        smoothness = Float.NaN;
        vectorSmoothness.set(Float.NaN, Float.NaN, Float.NaN);
        snap = false;
        restartX = restartY = restartZ = false;
        vectorMode = IVectorSink.Mode.RETARGET;
    }

    /** Reflects the target across the YZ plane: rotations (x, -y, -z, w), vectors (-x, y, z). */
    public void mirrorX()
    {
        PoseMath.mirrorX(rotation);
        PoseMath.mirrorX(pre);
        PoseMath.mirrorX(post);
        PoseMath.mirrorX(snapFrom);
        vector.x = -vector.x;
        vectorStart.x = -vectorStart.x;
        offset.x = -offset.x;
    }

    public void set(BoneTarget other)
    {
        hasPre = other.hasPre;
        pre.set(other.pre);
        hasPost = other.hasPost;
        post.set(other.post);
        offsetAdditive = other.offsetAdditive;
        vectorAdditive = other.vectorAdditive;
        hasSnapFrom = other.hasSnapFrom;
        snapFrom.set(other.snapFrom);
        hasVectorStart = other.hasVectorStart;
        vectorStart.set(other.vectorStart);
        hasRotation = other.hasRotation;
        rotation.set(other.rotation);
        hasOffset = other.hasOffset;
        offset.set(other.offset);
        hasVector = other.hasVector;
        vector.set(other.vector);
        smoothness = other.smoothness;
        vectorSmoothness.set(other.vectorSmoothness);
        snap = other.snap;
        restartX = other.restartX;
        restartY = other.restartY;
        restartZ = other.restartZ;
        vectorMode = other.vectorMode;
    }

}
