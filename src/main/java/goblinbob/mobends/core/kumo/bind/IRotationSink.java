package goblinbob.mobends.core.kumo.bind;

import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.IVec3fRead;

public interface IRotationSink extends IBoneSink
{

    /** @return the rotation the bone is currently heading towards (the smoothing target). */
    Quaternion getRotationTarget();

    /**
     * Sets a new rotation target.
     *
     * @param smoothness the damping rate per tick; NaN keeps whatever the bone had before.
     * @param snap       true to jump to the target immediately instead of smoothing towards it.
     * @param snapFrom   optional rotation to jump to first, so the smoothing starts from there.
     */
    void setRotationTarget(Quaternion target, float smoothness, boolean snap, Quaternion snapFrom);

    /** @return true if this bone also has an animation offset (model parts do, plain orientations do not). */
    boolean hasOffset();

    IVec3fRead getOffset();

    void setOffset(float x, float y, float z);

}
