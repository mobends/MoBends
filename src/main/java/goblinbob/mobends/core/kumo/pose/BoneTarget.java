package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.bind.IVectorSink;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

/** The per-frame target of one bone slot inside a {@link Pose}. */
public class BoneTarget
{

    public boolean hasRotation;
    public final Quaternion rotation = new Quaternion();

    public boolean hasOffset;
    public final Vec3f offset = new Vec3f();

    public boolean hasVector;
    public final Vec3f vector = new Vec3f();

    /** Damping per tick; NaN keeps whatever the bone had. For vectors per axis. */
    public float smoothness = Float.NaN;
    public final Vec3f vectorSmoothness = new Vec3f(Float.NaN, Float.NaN, Float.NaN);

    /** Jump to the rotation target this frame instead of smoothing. */
    public boolean snap;
    public IVectorSink.Mode vectorMode = IVectorSink.Mode.RETARGET;

    public void clear()
    {
        hasRotation = false;
        hasOffset = false;
        hasVector = false;
        smoothness = Float.NaN;
        vectorSmoothness.set(Float.NaN, Float.NaN, Float.NaN);
        snap = false;
        vectorMode = IVectorSink.Mode.RETARGET;
    }

    public void set(BoneTarget other)
    {
        hasRotation = other.hasRotation;
        rotation.set(other.rotation);
        hasOffset = other.hasOffset;
        offset.set(other.offset);
        hasVector = other.hasVector;
        vector.set(other.vector);
        smoothness = other.smoothness;
        vectorSmoothness.set(other.vectorSmoothness);
        snap = other.snap;
        vectorMode = other.vectorMode;
    }

}
