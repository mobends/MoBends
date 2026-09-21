package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.bind.IBoneSink;
import goblinbob.mobends.core.kumo.bind.IRotationSink;
import goblinbob.mobends.core.kumo.bind.IVectorSink;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.IVec3fRead;

/**
 * A buffer of per-bone targets. Layers evaluate into poses, poses composite into one another,
 * and the final pose is written to the subject's sinks once per frame.
 */
public class Pose
{

    /** How a rotation written into a slot combines with what is already there. */
    public enum Space
    {
        /** Replace. */
        OVERRIDE,
        /** Rotate in the parent's space: result = q * current (the {@code rotate*} idiom). */
        PRE,
        /** Rotate in the bone's own space: result = current * q (the {@code localRotate*} idiom). */
        POST,
    }

    private final Skeleton skeleton;
    private final BoneTarget[] targets;
    private final Quaternion temp = new Quaternion();

    /**
     * Whether an additive write to an empty slot composes onto the bone's live target (true for
     * the animator pose) or just records the rotation and its space for a later composite (layer
     * poses).
     */
    private final boolean sinkFallback;

    public Pose(Skeleton skeleton)
    {
        this(skeleton, false);
    }

    public Pose(Skeleton skeleton, boolean sinkFallback)
    {
        this.sinkFallback = sinkFallback;
        this.skeleton = skeleton;
        this.targets = new BoneTarget[skeleton.size()];
        for (int i = 0; i < targets.length; i++)
        {
            targets[i] = new BoneTarget();
        }
    }

    public Skeleton getSkeleton()
    {
        return skeleton;
    }

    public int size()
    {
        return targets.length;
    }

    public BoneTarget get(int index)
    {
        return targets[index];
    }

    public void clear()
    {
        for (BoneTarget target : targets)
        {
            target.clear();
        }
    }

    public void set(Pose other)
    {
        for (int i = 0; i < targets.length; i++)
        {
            targets[i].set(other.targets[i]);
        }
    }

    /**
     * The rotation a bone currently heads towards, as seen from this pose: the value written
     * earlier this frame, or else the bone's live target. Used as the base for additive writes.
     */
    public boolean currentRotation(int index, Quaternion dest)
    {
        BoneTarget target = targets[index];
        if (target.hasRotation)
        {
            dest.set(target.rotation);
            return true;
        }
        if (!sinkFallback)
        {
            return false;
        }
        IBoneSink sink = skeleton.sink(index);
        IRotationSink rotationSink = sink == null ? null : sink.asRotation();
        if (rotationSink != null)
        {
            dest.set(rotationSink.getRotationTarget());
            return true;
        }
        return false;
    }

    public void composeRotation(int index, Quaternion q, Space space)
    {
        BoneTarget target = targets[index];
        if (!target.hasRotation)
        {
            // First write: remember how it composes with whatever lies beneath.
            target.space = space;
        }
        else if (space == Space.OVERRIDE)
        {
            target.space = Space.OVERRIDE;
        }
        switch (space)
        {
            case PRE:
                if (currentRotation(index, temp))
                {
                    Quaternion.mul(q, temp, target.rotation);
                }
                else
                {
                    target.rotation.set(q);
                }
                break;
            case POST:
                if (currentRotation(index, temp))
                {
                    Quaternion.mul(temp, q, target.rotation);
                }
                else
                {
                    target.rotation.set(q);
                }
                break;
            case OVERRIDE:
            default:
                target.rotation.set(q);
                break;
        }
        target.hasRotation = true;
    }

    public void composeOffset(int index, float x, float y, float z, Space space)
    {
        BoneTarget target = targets[index];
        if (!target.hasOffset && !target.hasRotation) target.space = space;
        if (space == Space.OVERRIDE || !target.hasOffset)
        {
            if (space != Space.OVERRIDE && sinkFallback)
            {
                IBoneSink sink = skeleton.sink(index);
                IRotationSink rotationSink = sink == null ? null : sink.asRotation();
                if (rotationSink != null && rotationSink.hasOffset())
                {
                    IVec3fRead current = rotationSink.getOffset();
                    target.offset.set(current.getX() + x, current.getY() + y, current.getZ() + z);
                    target.hasOffset = true;
                    return;
                }
            }
            target.offset.set(x, y, z);
        }
        else
        {
            target.offset.add(x, y, z);
        }
        target.hasOffset = true;
    }

    public void composeVector(int index, float x, float y, float z, Space space)
    {
        BoneTarget target = targets[index];
        if (!target.hasVector) target.space = space;
        else if (space == Space.OVERRIDE) target.space = Space.OVERRIDE;
        if (space == Space.OVERRIDE || !target.hasVector)
        {
            if (space != Space.OVERRIDE && sinkFallback)
            {
                IBoneSink sink = skeleton.sink(index);
                IVectorSink vectorSink = sink == null ? null : sink.asVector();
                if (vectorSink != null)
                {
                    IVec3fRead current = vectorSink.getVectorTarget();
                    target.vector.set(current.getX() + x, current.getY() + y, current.getZ() + z);
                    target.hasVector = true;
                    return;
                }
            }
            target.vector.set(x, y, z);
        }
        else
        {
            target.vector.add(x, y, z);
        }
        target.hasVector = true;
    }

    /** Writes every target that was set this frame into the bound sinks. */
    public void writeTo(Skeleton skeleton)
    {
        for (int i = 0; i < targets.length; i++)
        {
            BoneTarget target = targets[i];
            IBoneSink sink = skeleton.sink(i);
            if (sink == null)
            {
                continue;
            }

            IRotationSink rotationSink = sink.asRotation();
            if (rotationSink != null)
            {
                if (target.hasRotation)
                {
                    rotationSink.setRotationTarget(target.rotation, target.smoothness, target.snap, target.hasSnapFrom ? target.snapFrom : null);
                }
                if (target.hasOffset && rotationSink.hasOffset())
                {
                    rotationSink.setOffset(target.offset.x, target.offset.y, target.offset.z);
                }
            }

            IVectorSink vectorSink = sink.asVector();
            if (vectorSink != null && target.hasVector)
            {
                vectorSink.setVectorTarget(target.vector.x, target.vector.y, target.vector.z,
                        target.vectorSmoothness.x, target.vectorSmoothness.y, target.vectorSmoothness.z,
                        target.vectorMode, target.hasVectorStart ? target.vectorStart : null);
            }
        }
    }

}
