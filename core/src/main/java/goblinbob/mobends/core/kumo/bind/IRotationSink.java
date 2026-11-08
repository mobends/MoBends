package goblinbob.mobends.core.kumo.bind;

import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.IVec3fRead;

public interface IRotationSink extends IBoneSink
{

    /** @return the rotation the bone is currently heading towards (the smoothing target). */
    Quaternion getRotationTarget();

    /**
     * @return the rotation the bone is drawn at, smoothed towards its targets (as of the last
     *         frame); a sink that doesn't smooth is drawn at its target.
     */
    default Quaternion getRotationShown()
    {
        return getRotationTarget();
    }

    /**
     * Sets a new rotation target.
     *
     * @param smoothing  ticks to reach the target (0: at once); NaN for the bone's base smoothing (nothing smoothed it this frame).
     * @param snap       true to jump to the target immediately instead of smoothing towards it.
     * @param snapFrom   optional rotation to jump to first, so the smoothing starts from there.
     */
    void setRotationTarget(Quaternion target, float smoothing, boolean snap, Quaternion snapFrom);

    /** @return true if this bone also has an animation offset (model parts do, plain orientations do not). */
    boolean hasOffset();

    /** @return the offset the bone is heading towards (the smoothing target). */
    IVec3fRead getOffset();

    /**
     * Sets a new offset target, as {@link IVectorSink#setVectorTarget} a vector's; a NaN component
     * keeps that axis's current target. An axis with no smoothing (NaN) gets the part's base
     * smoothing; unlike a vector's, that is none by default, and such an axis in
     * {@link IVectorSink.Mode#RETARGET} is applied as is: offsets are smoothed only while an item smooths them.
     */
    void setOffsetTarget(float x, float y, float z, float smoothingX, float smoothingY, float smoothingZ, IVectorSink.Mode mode, IVec3fRead start);

    /** Restarts the offset's slide on the given axes from the current value (a target replaced within the frame). */
    default void restartOffsetSlide(boolean x, boolean y, boolean z)
    {
    }

    /** @return true if the bone can be scaled (a model part can; plain orientations can't). */
    default boolean hasScale()
    {
        return false;
    }

    /** @return the bone's scale, per axis; only called where {@link #hasScale}. */
    default IVec3fRead getScale()
    {
        throw new UnsupportedOperationException("This bone can't be scaled.");
    }

    /** Sets the bone's scale, applied as is (scales aren't smoothed); a NaN component keeps that axis's current scale. */
    default void setScaleTarget(float x, float y, float z)
    {
    }

}
