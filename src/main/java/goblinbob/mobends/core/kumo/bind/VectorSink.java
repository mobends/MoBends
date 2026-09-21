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
    public void setVectorTarget(float x, float y, float z, float smoothnessX, float smoothnessY, float smoothnessZ, Mode mode, IVec3fRead start)
    {
        // A NaN component means "this axis is not written": keep its current target (slideY() idiom).
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
                slideAxis(EnumAxis.X, x, smoothnessX);
                slideAxis(EnumAxis.Y, y, smoothnessY);
                slideAxis(EnumAxis.Z, z, smoothnessZ);
                break;
            case RETARGET:
            default:
                vector.retarget(x, y, z);
                if (!Float.isNaN(smoothnessX)) vector.smoothness.x = smoothnessX;
                if (!Float.isNaN(smoothnessY)) vector.smoothness.y = smoothnessY;
                if (!Float.isNaN(smoothnessZ)) vector.smoothness.z = smoothnessZ;
                break;
        }
    }

    private void slideAxis(EnumAxis axis, float value, float smoothness)
    {
        float s = Float.isNaN(smoothness) ? vector.getSmoothness(axis) : smoothness;
        vector.slideTo(axis, value, s);
    }

}
