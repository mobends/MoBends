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
    private final SmoothVector3f smoothOffset;

    public OrientationSink(SmoothOrientation rotation, IVec3f offset)
    {
        this(rotation, offset, null);
    }

    /** @param smoothOffset the offset's smoothing state; null to write offsets as they come. */
    public OrientationSink(SmoothOrientation rotation, IVec3f offset, @Nullable SmoothVector3f smoothOffset)
    {
        this.rotation = rotation;
        this.offset = offset;
        this.smoothOffset = offset == null ? null : smoothOffset;
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
    public void setRotationTarget(Quaternion target, float smoothing, boolean snap, Quaternion snapFrom)
    {
        if (!Float.isNaN(smoothing))
        {
            rotation.setSmoothing(smoothing);
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
        return smoothOffset != null ? smoothOffset.end : offset;
    }

    @Override
    public void setOffsetTarget(float x, float y, float z, float smoothingX, float smoothingY, float smoothingZ, IVectorSink.Mode mode, IVec3fRead start)
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
        if (smoothOffset == null)
        {
            offset.set(x, y, z);
            return;
        }
        if (start != null && mode != IVectorSink.Mode.SNAP)
        {
            smoothOffset.set(start.getX(), start.getY(), start.getZ());
        }
        switch (mode)
        {
            case SNAP:
                smoothOffset.set(x, y, z);
                break;
            case SLIDE:
                slideAxis(EnumAxis.X, x, smoothingX);
                slideAxis(EnumAxis.Y, y, smoothingY);
                slideAxis(EnumAxis.Z, z, smoothingZ);
                break;
            case RETARGET:
            default:
                retargetAxis(EnumAxis.X, x, smoothingX);
                retargetAxis(EnumAxis.Y, y, smoothingY);
                retargetAxis(EnumAxis.Z, z, smoothingZ);
                break;
        }
        offset.set(smoothOffset.getX(), smoothOffset.getY(), smoothOffset.getZ());
    }

    private void slideAxis(EnumAxis axis, float value, float smoothing)
    {
        float s = Float.isNaN(smoothing) ? smoothOffset.getSmoothing(axis) : smoothing;
        smoothOffset.slideTo(axis, value, s);
    }

    /** An unsmoothed axis is applied as is. */
    private void retargetAxis(EnumAxis axis, float value, float smoothing)
    {
        if (Float.isNaN(smoothing))
        {
            smoothOffset.set(axis, value);
        }
        else
        {
            smoothOffset.retarget(axis, value, smoothing);
        }
    }

    @Override
    public void restartOffsetSlide(boolean x, boolean y, boolean z)
    {
        if (smoothOffset == null)
        {
            return;
        }
        if (x) smoothOffset.restart(EnumAxis.X);
        if (y) smoothOffset.restart(EnumAxis.Y);
        if (z) smoothOffset.restart(EnumAxis.Z);
    }

}
