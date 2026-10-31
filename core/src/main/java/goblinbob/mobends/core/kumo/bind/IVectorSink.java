package goblinbob.mobends.core.kumo.bind;

import goblinbob.mobends.core.math.vector.IVec3fRead;

public interface IVectorSink extends IBoneSink
{

    /** How a vector target is applied to the smoothing state. */
    enum Mode
    {
        /**
         * Restart the interpolation from the current value every time (an exponential approach
         * to the target).
         */
        RETARGET,
        /** Restart only when the target changes (a linear tween of {@code smoothing} ticks). */
        SLIDE,
        /** Jump to the target immediately. */
        SNAP,
    }

    IVec3fRead getVectorTarget();

    /**
     * @param smoothingX/Y/Z ticks to reach the target, per axis (0: at once); NaN keeps the previous value for that axis.
     * @param start optional value to restart the interpolation from (RETARGET / SLIDE only).
     */
    void setVectorTarget(float x, float y, float z, float smoothingX, float smoothingY, float smoothingZ, Mode mode, IVec3fRead start);


    /** Restarts the slide on the given axes from the current value (a target replaced within the frame). */
    default void restartSlide(boolean x, boolean y, boolean z)
    {
    }

}
