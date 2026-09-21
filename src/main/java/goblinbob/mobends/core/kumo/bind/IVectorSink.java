package goblinbob.mobends.core.kumo.bind;

import goblinbob.mobends.core.math.vector.IVec3fRead;

public interface IVectorSink extends IBoneSink
{

    /** How a vector target is applied to the smoothing state. */
    enum Mode
    {
        /**
         * Restart the interpolation from the current value every time (an exponential approach
         * to the target). This is what happens in the original code whenever several bits write
         * the same vector in one frame.
         */
        RETARGET,
        /** Restart only when the target changes (a linear tween of 1/smoothness ticks). */
        SLIDE,
        /** Jump to the target immediately. */
        SNAP,
    }

    IVec3fRead getVectorTarget();

    /**
     * @param smoothnessX/Y/Z damping per axis; NaN keeps the previous value for that axis.
     */
    void setVectorTarget(float x, float y, float z, float smoothnessX, float smoothnessY, float smoothnessZ, Mode mode);

}
