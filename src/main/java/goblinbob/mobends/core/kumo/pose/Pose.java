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
     * Becomes the left-right mirror image of {@code other}: slot {@code i} of the source lands in
     * {@code pairOf[i]} with Y and Z rotations and X offsets negated.
     */
    public void mirrorFrom(Pose other, int[] pairOf)
    {
        mirrorFrom(other, pairOf, true);
    }

    /** As {@link #mirrorFrom(Pose, int[])}; with {@code flip} false only the slots are swapped. */
    public void mirrorFrom(Pose other, int[] pairOf, boolean flip)
    {
        // Poses created before the skeleton finished growing can be shorter than the pairing.
        int count = Math.min(targets.length, other.targets.length);
        for (int i = 0; i < count; i++)
        {
            int j = i < pairOf.length ? pairOf[i] : i;
            if (j >= targets.length) j = i;
            targets[j].set(other.targets[i]);
            if (flip)
            {
                targets[j].mirrorX();
            }
        }
    }

    /** The live target of a bone's sink (the value beneath the first layer), if it has one. */
    private boolean sinkRotation(int index, Quaternion dest)
    {
        IBoneSink sink = skeleton.sink(index);
        IRotationSink rotationSink = sink == null ? null : sink.asRotation();
        if (rotationSink != null)
        {
            dest.set(rotationSink.getRotationTarget());
            return true;
        }
        return false;
    }

    /**
     * Composes a rotation into a slot. OVERRIDE replaces (folding away relative parts); PRE and
     * POST either fold into an existing absolute value, or accumulate as relative parts to be
     * applied to whatever lies beneath at composite / write time. The animator pose resolves
     * relative parts against the bone's live target right away.
     */
    public void composeRotation(int index, Quaternion q, Space space)
    {
        BoneTarget target = targets[index];
        switch (space)
        {
            case PRE:
                if (target.hasRotation)
                {
                    Quaternion.mul(q, target.rotation, temp);
                    target.rotation.set(temp);
                }
                else if (sinkFallback && sinkRotation(index, temp))
                {
                    Quaternion.mul(q, temp, target.rotation);
                    target.hasRotation = true;
                }
                else if (target.hasPre)
                {
                    Quaternion.mul(q, target.pre, temp);
                    target.pre.set(temp);
                }
                else
                {
                    target.pre.set(q);
                    target.hasPre = true;
                }
                break;
            case POST:
                if (target.hasRotation)
                {
                    Quaternion.mul(target.rotation, q, temp);
                    target.rotation.set(temp);
                }
                else if (sinkFallback && sinkRotation(index, temp))
                {
                    Quaternion.mul(temp, q, target.rotation);
                    target.hasRotation = true;
                }
                else if (target.hasPost)
                {
                    Quaternion.mul(target.post, q, temp);
                    target.post.set(temp);
                }
                else
                {
                    target.post.set(q);
                    target.hasPost = true;
                }
                break;
            case OVERRIDE:
            default:
                target.rotation.set(q);
                target.hasRotation = true;
                target.hasPre = false;
                target.hasPost = false;
                break;
        }
    }

    /** Applies another slot's rotation parts onto this pose's slot (layer compositing). */
    public void composeRotation(int index, BoneTarget src)
    {
        if (src.hasRotation)
        {
            composeRotation(index, src.rotation, Space.OVERRIDE);
        }
        else
        {
            if (src.hasPre) composeRotation(index, src.pre, Space.PRE);
            if (src.hasPost) composeRotation(index, src.post, Space.POST);
        }
    }

    public void composeOffset(int index, float x, float y, float z, Space space)
    {
        BoneTarget target = targets[index];
        if (space == Space.OVERRIDE)
        {
            target.offset.set(x, y, z);
            target.offsetAdditive = false;
        }
        else if (target.hasOffset)
        {
            target.offset.add(x, y, z);
        }
        else if (sinkFallback)
        {
            IBoneSink sink = skeleton.sink(index);
            IRotationSink rotationSink = sink == null ? null : sink.asRotation();
            if (rotationSink != null && rotationSink.hasOffset())
            {
                IVec3fRead current = rotationSink.getOffset();
                target.offset.set(current.getX() + x, current.getY() + y, current.getZ() + z);
            }
            else
            {
                target.offset.set(x, y, z);
            }
            target.offsetAdditive = false;
        }
        else
        {
            target.offset.set(x, y, z);
            target.offsetAdditive = true;
        }
        target.hasOffset = true;
    }

    public void composeVector(int index, float x, float y, float z, Space space)
    {
        BoneTarget target = targets[index];
        if (space == Space.OVERRIDE)
        {
            if (target.hasVector && target.vectorMode == IVectorSink.Mode.SNAP)
            {
                // A snap followed by another write in the same frame: the new interpolation
                // starts from the snapped value (setY(...) then slideY(...) in the original code).
                target.hasVectorStart = true;
                target.vectorStart.set(target.vector);
            }
            else if (target.hasVector)
            {
                // Two writers with different targets in one frame: each slideTo() saw a new
                // target and restarted from the current value.
                if (!Float.isNaN(x) && x != target.vector.x) target.restartX = true;
                if (!Float.isNaN(y) && y != target.vector.y) target.restartY = true;
                if (!Float.isNaN(z) && z != target.vector.z) target.restartZ = true;
            }
            target.vector.set(x, y, z);
            target.vectorAdditive = false;
        }
        else if (target.hasVector)
        {
            target.vector.add(x, y, z);
        }
        else if (sinkFallback)
        {
            IBoneSink sink = skeleton.sink(index);
            IVectorSink vectorSink = sink == null ? null : sink.asVector();
            if (vectorSink != null)
            {
                IVec3fRead current = vectorSink.getVectorTarget();
                target.vector.set(current.getX() + x, current.getY() + y, current.getZ() + z);
            }
            else
            {
                target.vector.set(x, y, z);
            }
            target.vectorAdditive = false;
        }
        else
        {
            target.vector.set(x, y, z);
            target.vectorAdditive = true;
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
                if (target.vectorMode == IVectorSink.Mode.SLIDE && (target.restartX || target.restartY || target.restartZ))
                {
                    vectorSink.restartSlide(target.restartX, target.restartY, target.restartZ);
                }
            }
        }
    }

}
