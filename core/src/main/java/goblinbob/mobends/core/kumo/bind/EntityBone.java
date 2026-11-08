package goblinbob.mobends.core.kumo.bind;

import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.IVec3fRead;
import goblinbob.mobends.core.math.vector.SmoothVector3f;

/**
 * One of the entity's own bones ({@code root}, {@code center}, see
 * {@link goblinbob.mobends.core.kumo.pose.Skeleton#isEntityBone}): a smoothed position, in model
 * units, and a smoothed rotation. Unlike a part's offset, the position keeps a base smoothing (see
 * {@link VectorSink}). Its owner updates it every frame and draws the model through it.
 */
public class EntityBone implements IRotationSink, IVectorSink
{

    public final SmoothVector3f position = new SmoothVector3f();
    public final SmoothOrientation rotation = new SmoothOrientation();

    private final VectorSink positionSink = new VectorSink(position);
    private final OrientationSink rotationSink = new OrientationSink(rotation, null);

    /** Moves the position and the rotation towards their targets. */
    public void update(float ticksPerFrame)
    {
        position.update(ticksPerFrame);
        rotation.update(ticksPerFrame);
    }

    /** Sets the base smoothing (ticks) of the rotation and of each axis of the position, and their current smoothing. */
    public void setBaseSmoothing(float rotationTicks, float x, float y, float z)
    {
        rotation.setSmoothing(rotationTicks).setBaseSmoothing(rotationTicks);
        position.smoothing.set(x, y, z);
        position.baseSmoothing.set(x, y, z);
    }

    @Override
    public IRotationSink asRotation()
    {
        return this;
    }

    @Override
    public IVectorSink asVector()
    {
        return this;
    }

    @Override
    public Quaternion getRotationTarget()
    {
        return rotationSink.getRotationTarget();
    }

    @Override
    public Quaternion getRotationShown()
    {
        return rotationSink.getRotationShown();
    }

    @Override
    public void setRotationTarget(Quaternion target, float smoothing, boolean snap, Quaternion snapFrom)
    {
        rotationSink.setRotationTarget(target, smoothing, snap, snapFrom);
    }

    /** Its position is a vector ({@link #getVectorTarget}), not a part's offset. */
    @Override
    public boolean hasOffset()
    {
        return false;
    }

    @Override
    public IVec3fRead getOffset()
    {
        return null;
    }

    @Override
    public void setOffsetTarget(float x, float y, float z, float smoothingX, float smoothingY, float smoothingZ, Mode mode, IVec3fRead start)
    {
    }

    @Override
    public IVec3fRead getVectorTarget()
    {
        return positionSink.getVectorTarget();
    }

    @Override
    public void setVectorTarget(float x, float y, float z, float smoothingX, float smoothingY, float smoothingZ, Mode mode, IVec3fRead start)
    {
        positionSink.setVectorTarget(x, y, z, smoothingX, smoothingY, smoothingZ, mode, start);
    }

    @Override
    public void restartSlide(boolean x, boolean y, boolean z)
    {
        positionSink.restartSlide(x, y, z);
    }

}
