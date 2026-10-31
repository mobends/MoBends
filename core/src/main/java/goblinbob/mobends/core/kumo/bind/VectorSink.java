package goblinbob.mobends.core.kumo.bind;

import goblinbob.mobends.core.math.vector.IVec3fRead;
import goblinbob.mobends.core.math.vector.SmoothVector3f;
import goblinbob.mobends.core.util.EnumAxis;

/** Sink backed by a {@link SmoothVector3f} (e.g. the entity's global offset). */
public class VectorSink implements IVectorSink
{

    private final SmoothVector3f vector;

    public VectorSink(SmoothVector3f vector)
    {
        this.vector = vector;
    }

    @Override
    public IRotationSink asRotation()
    {
        return null;
    }

    @Override
    public IVectorSink asVector()
    {
        return this;
    }

    @Override
    public IVec3fRead getVectorTarget()
    {
        return vector.end;
    }

    @Override
    public void setVectorTarget(float x, float y, float z, float smoothingX, float smoothingY, float smoothingZ, Mode mode, IVec3fRead start)
    {
        // A NaN component means "this axis is not written": keep its current target (per axis).
        if (Float.isNaN(x)) x = vector.end.x;
        if (Float.isNaN(y)) y = vector.end.y;
        if (Float.isNaN(z)) z = vector.end.z;
        if (start != null && mode != Mode.SNAP)
        {
            vector.set(start.getX(), start.getY(), start.getZ());
        }
        switch (mode)
        {
            case SNAP:
                vector.set(x, y, z);
                break;
            case SLIDE:
                slideAxis(EnumAxis.X, x, smoothingX);
                slideAxis(EnumAxis.Y, y, smoothingY);
                slideAxis(EnumAxis.Z, z, smoothingZ);
                break;
            case RETARGET:
            default:
                if (!Float.isNaN(smoothingX)) vector.smoothing.x = smoothingX;
                if (!Float.isNaN(smoothingY)) vector.smoothing.y = smoothingY;
                if (!Float.isNaN(smoothingZ)) vector.smoothing.z = smoothingZ;
                vector.retarget(x, y, z);
                break;
        }
    }

    private void slideAxis(EnumAxis axis, float value, float smoothing)
    {
        float s = Float.isNaN(smoothing) ? vector.getSmoothing(axis) : smoothing;
        vector.slideTo(axis, value, s);
    }


    @Override
    public void restartSlide(boolean x, boolean y, boolean z)
    {
        if (x) vector.restart(EnumAxis.X);
        if (y) vector.restart(EnumAxis.Y);
        if (z) vector.restart(EnumAxis.Z);
    }

}
