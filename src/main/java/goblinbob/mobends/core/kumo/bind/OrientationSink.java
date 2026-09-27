package goblinbob.mobends.core.kumo.bind;

import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.IVec3f;
import goblinbob.mobends.core.math.vector.IVec3fRead;

/**
 * Sink backed by a {@link SmoothOrientation} and an optional plain offset vector. Writing a
 * target goes through the same code path the procedural animations use ({@code orient*}), so the
 * damping behaviour is identical.
 */
public class OrientationSink implements IRotationSink
{

    private final SmoothOrientation rotation;
    private final IVec3f offset;

    public OrientationSink(SmoothOrientation rotation, IVec3f offset)
    {
        this.rotation = rotation;
        this.offset = offset;
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
        return offset;
    }

    @Override
    public void setOffset(float x, float y, float z)
    {
        if (offset != null)
        {
            offset.set(x, y, z);
        }
    }

}
