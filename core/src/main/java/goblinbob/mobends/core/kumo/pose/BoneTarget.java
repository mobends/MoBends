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
     * {@code pre * beneath * post}, where "beneath" is the earlier layers' value or the rest
     * pose. An OVERRIDE write folds them away.
     */
    public boolean hasPre;
    public final Quaternion pre = new Quaternion();
    public boolean hasPost;
    public final Quaternion post = new Quaternion();

    /** Offset / vector written additively (relative to what lies beneath) rather than absolutely. */
    public boolean offsetAdditive;
    public boolean vectorAdditive;

    /** Rotation the bone is snapped to before the target is applied, so smoothing starts from there. */
    public boolean hasSnapFrom;
    public final Quaternion snapFrom = new Quaternion();

    /**
     * Vector (or, on a part, offset) value the interpolation restarts from (a snap write followed by
     * a retarget in one frame).
     */
    public boolean hasVectorStart;
    public final Vec3f vectorStart = new Vec3f();

    public boolean hasOffset;
    public final Vec3f offset = new Vec3f();

    public boolean hasVector;
    public final Vec3f vector = new Vec3f();

    /**
     * A part's scale, per axis (1: as modelled). Relative ({@link #scaleRelative}: a PRE / POST
     * write while no absolute value exists) it is a factor on what lies beneath.
     */
    public boolean hasScale;
    public boolean scaleRelative;
    public final Vec3f scale = new Vec3f(1, 1, 1);

    /** Ticks to reach the target (0: at once); NaN, where nothing smooths the bone this frame, for its base smoothing. For vectors per axis. */
    public float smoothing = Float.NaN;
    public final Vec3f vectorSmoothing = new Vec3f(Float.NaN, Float.NaN, Float.NaN);
    /**
     * A part's offset smoothing, in ticks per axis. Unlike a rotation's it lasts one frame: an axis
     * with no smoothing (NaN) is applied as is.
     */
    public final Vec3f offsetSmoothing = new Vec3f(Float.NaN, Float.NaN, Float.NaN);

    /** Jump to the rotation target this frame instead of smoothing. */
    public boolean snap;
    /**
     * Set when a vector (or, on a part, offset) write replaced a different target written earlier
     * in the same frame: the sink restarts the slide from the current value on these axes (an
     * exponential approach instead of a linear tween).
     */
    public boolean restartX, restartY, restartZ;
    public IVectorSink.Mode vectorMode = IVectorSink.Mode.RETARGET;
    /** How a part's offset target is applied, as {@link #vectorMode} for a vector. */
    public IVectorSink.Mode offsetMode = IVectorSink.Mode.RETARGET;

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
        hasScale = false;
        scaleRelative = false;
        smoothing = Float.NaN;
        vectorSmoothing.set(Float.NaN, Float.NaN, Float.NaN);
        offsetSmoothing.set(Float.NaN, Float.NaN, Float.NaN);
        snap = false;
        restartX = restartY = restartZ = false;
        vectorMode = IVectorSink.Mode.RETARGET;
        offsetMode = IVectorSink.Mode.RETARGET;
    }

    /** Reflects the target across the YZ plane: rotations (x, -y, -z, w), vectors (-x, y, z); a scale stays as it is. */
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
        hasScale = other.hasScale;
        scaleRelative = other.scaleRelative;
        scale.set(other.scale);
        smoothing = other.smoothing;
        vectorSmoothing.set(other.vectorSmoothing);
        offsetSmoothing.set(other.offsetSmoothing);
        snap = other.snap;
        restartX = other.restartX;
        restartY = other.restartY;
        restartZ = other.restartZ;
        vectorMode = other.vectorMode;
        offsetMode = other.offsetMode;
    }

}
