package goblinbob.mobends.core.kumo.bind;

import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.IVec3f;
import goblinbob.mobends.core.math.vector.IVec3fRead;
import goblinbob.mobends.core.math.vector.SmoothVector3f;
import goblinbob.mobends.core.util.EnumAxis;

import javax.annotation.Nullable;

/**
 * Sink backed by a {@link SmoothOrientation} and an optional offset vector: the orientation
 * smooths towards each target written, and the offset too where the part keeps a smoothing for it
 * (whoever owns the smoothing updates it, and carries its value into the offset).
 */
public class OrientationSink implements IRotationSink
{

    private final SmoothOrientation rotation;
    private final IVec3f offset;
    @Nullable
    private final SmoothVector3f offsetSmoothing;

    public OrientationSink(SmoothOrientation rotation, IVec3f offset)
    {
        this(rotation, offset, null);
    }

    /** @param offsetSmoothing the offset's smoothing state; null to write offsets as they come. */
    public OrientationSink(SmoothOrientation rotation, IVec3f offset, @Nullable SmoothVector3f offsetSmoothing)
    {
        this.rotation = rotation;
        this.offset = offset;
        this.offsetSmoothing = offset == null ? null : offsetSmoothing;
    }

    @Override
    public IRotationSink asRotation()
    {
        return this;
    }

    @Override
    public IVectorSink asVector()
    {
        return null;
    }

    @Override
    public Quaternion getRotationTarget()
    {
        return rotation.getEnd();
    }

    @Override
    public Quaternion getRotationShown()
    {
        return rotation.getSmooth();
    }

    @Override
    public void setRotationTarget(Quaternion target, float smoothness, boolean snap, Quaternion snapFrom)
    {
        if (!Float.isNaN(smoothness))
        {
            rotation.setSmoothness(smoothness);
        }

        if (snapFrom != null)
        {
            rotation.snapTo(snapFrom);
        }

        if (snap)
        {
            rotation.snapTo(target);
        }
        else
        {
            rotation.target(target);
        }
    }

    @Override
    public boolean hasOffset()
    {
        return offset != null;
    }

    @Override
    public IVec3fRead getOffset()
    {
        return offsetSmoothing != null ? offsetSmoothing.end : offset;
    }

    @Override
    public void setOffsetTarget(float x, float y, float z, float smoothnessX, float smoothnessY, float smoothnessZ, IVectorSink.Mode mode, IVec3fRead start)
    {
        if (offset == null)
        {
            return;
        }
        // A NaN component means "this axis is not written": keep its current target (per axis).
        IVec3fRead current = getOffset();
        if (Float.isNaN(x)) x = current.getX();
        if (Float.isNaN(y)) y = current.getY();
        if (Float.isNaN(z)) z = current.getZ();
        if (offsetSmoothing == null)
        {
            offset.set(x, y, z);
            return;
        }
        if (start != null && mode != IVectorSink.Mode.SNAP)
        {
            offsetSmoothing.set(start.getX(), start.getY(), start.getZ());
        }
        switch (mode)
        {
            case SNAP:
                offsetSmoothing.set(x, y, z);
                break;
            case SLIDE:
                slideAxis(EnumAxis.X, x, smoothnessX);
                slideAxis(EnumAxis.Y, y, smoothnessY);
                slideAxis(EnumAxis.Z, z, smoothnessZ);
                break;
            case RETARGET:
            default:
                retargetAxis(EnumAxis.X, x, smoothnessX);
                retargetAxis(EnumAxis.Y, y, smoothnessY);
                retargetAxis(EnumAxis.Z, z, smoothnessZ);
                break;
        }
        offset.set(offsetSmoothing.getX(), offsetSmoothing.getY(), offsetSmoothing.getZ());
    }

    private void slideAxis(EnumAxis axis, float value, float smoothness)
    {
        float s = Float.isNaN(smoothness) ? offsetSmoothing.getSmoothness(axis) : smoothness;
        offsetSmoothing.slideTo(axis, value, s);
    }

    /** An undamped axis is applied as is. */
    private void retargetAxis(EnumAxis axis, float value, float smoothness)
    {
        if (Float.isNaN(smoothness))
        {
            offsetSmoothing.set(axis, value);
        }
        else
        {
            offsetSmoothing.retarget(axis, value, smoothness);
        }
    }

    @Override
    public void restartOffsetSlide(boolean x, boolean y, boolean z)
    {
        if (offsetSmoothing == null)
        {
            return;
        }
        if (x) offsetSmoothing.restart(EnumAxis.X);
        if (y) offsetSmoothing.restart(EnumAxis.Y);
        if (z) offsetSmoothing.restart(EnumAxis.Z);
    }

}
