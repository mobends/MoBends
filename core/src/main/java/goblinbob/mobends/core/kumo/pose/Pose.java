package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.state.EntityState;
import javax.annotation.Nullable;
import goblinbob.mobends.core.kumo.bind.IBoneSink;
import goblinbob.mobends.core.kumo.bind.IRotationSink;
import goblinbob.mobends.core.kumo.bind.IVectorSink;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

/**
 * A buffer of per-bone targets. Layers evaluate into poses, poses composite into one another,
 * and the final pose is written to the subject's sinks once per frame.
 *
 * <p>Poses are declarative: beneath the first layer is the rest pose (no rotation, no offset, a
 * scale of one, no vector), never what an earlier frame left in the bones. A relative write with
 * nothing beneath it builds on the rest pose, and every channel nothing writes in a frame goes back
 * to rest (smoothed by the bone's base smoothing).
 */
public class Pose
{

    /** How a rotation written into a slot combines with what is already there. */
    public enum Space
    {
        /** Replace. */
        OVERRIDE,
        /** Rotate in the parent's space: result = q * current. */
        PRE,
        /** Rotate in the bone's own space: result = current * q. */
        POST,
    }

    private final Skeleton skeleton;
    private final BoneTarget[] targets;
    private final Quaternion temp = new Quaternion();
    private final Vec3f offsetTemp = new Vec3f();

    /**
     * Whether this is a base pose (the animator pose), with the rest pose beneath it: a relative write
     * to an empty slot resolves against rest at once. Other poses (a layer's) record the relative
     * part, for whatever they are composited onto.
     */
    private final boolean base;
    /** The entity state whose bones this pose reads and writes; null for a scratch pose, which doesn't. */
    @Nullable
    private final EntityState owner;
    /** The pose this one goes on top of (a layer's: the animator pose, holding the layers below), or null. */
    private Pose below;

    public Pose(Skeleton skeleton)
    {
        this(skeleton, false);
    }

    public Pose(Skeleton skeleton, boolean base)
    {
        this(skeleton, base, null);
    }

    /** A pose of {@code owner}, an entity's state: it reads and writes that entity's bones (see {@link #writeTo}). */
    public Pose(Skeleton skeleton, boolean base, @Nullable EntityState owner)
    {
        this.owner = owner;
        this.base = base;
        this.skeleton = skeleton;
        this.targets = new BoneTarget[skeleton.size()];
        for (int i = 0; i < targets.length; i++)
        {
            targets[i] = new BoneTarget();
        }
    }

    /** Sets the pose this one goes on top of, for {@link #rotationSoFar} and the like; null for none. */
    public void setBelow(Pose below)
    {
        this.below = below;
    }

    /** Whether this is a base pose, with the rest pose beneath it (see {@link #base}). */
    public boolean isBase()
    {
        return base;
    }

    /** The pose this one goes on top of, or null (see {@link #setBelow}). */
    public Pose getBelow()
    {
        return below;
    }

    /**
     * The rotation a slot has so far this frame: its own, with its relative parts applied to what
     * the pose below has so far. Where nothing has written the bone this frame, the rest rotation
     * (identity).
     */
    public void rotationSoFar(int index, Quaternion dest)
    {
        BoneTarget target = targets[index];
        if (target.hasRotation)
        {
            dest.set(target.rotation);
            return;
        }
        if (below != null)
        {
            below.rotationSoFar(index, dest);
        }
        else
        {
            dest.setIdentity();
        }
        if (target.hasPre)
        {
            Quaternion.mul(target.pre, dest, temp);
            dest.set(temp);
        }
        if (target.hasPost)
        {
            Quaternion.mul(dest, target.post, temp);
            dest.set(temp);
        }
    }

    /** As {@link #rotationSoFar}, for the slot's offset (zero where nothing has written it). */
    public void offsetSoFar(int index, Vec3f dest)
    {
        BoneTarget target = targets[index];
        if (target.hasOffset && !target.offsetAdditive)
        {
            dest.set(target.offset);
            return;
        }
        if (below != null)
        {
            below.offsetSoFar(index, dest);
        }
        else
        {
            dest.set(0F, 0F, 0F);
        }
        if (target.hasOffset)
        {
            dest.add(target.offset.x, target.offset.y, target.offset.z);
        }
    }

    /** As {@link #rotationSoFar}, for the slot's vector (zero where nothing has written it). */
    public void vectorSoFar(int index, Vec3f dest)
    {
        BoneTarget target = targets[index];
        if (target.hasVector && !target.vectorAdditive)
        {
            dest.set(target.vector);
            return;
        }
        if (below != null)
        {
            below.vectorSoFar(index, dest);
        }
        else
        {
            dest.set(0F, 0F, 0F);
        }
        if (target.hasVector)
        {
            dest.add(target.vector.x, target.vector.y, target.vector.z);
        }
    }

    /** As {@link #rotationSoFar}, for the slot's scale (one where nothing has written it). */
    public void scaleSoFar(int index, Vec3f dest)
    {
        BoneTarget target = targets[index];
        if (target.hasScale && !target.scaleRelative)
        {
            dest.set(target.scale);
            return;
        }
        if (below != null)
        {
            below.scaleSoFar(index, dest);
        }
        else
        {
            dest.set(1F, 1F, 1F);
        }
        if (target.hasScale)
        {
            dest.scale(target.scale.x, target.scale.y, target.scale.z);
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
     * {@code pairOf[i]}, with Y and Z rotations and X offsets negated when {@code flip} is set.
     */
    public void mirrorFrom(Pose other, int[] pairOf, boolean flip)
    {
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

    /**
     * The absolute rotation of a slot: its own, or else, in a base pose, what it has over the rest
     * pose.
     *
     * @return false when there is none (a slot of a layer's pose written only relatively, or not at all)
     */
    public boolean absoluteRotation(int index, Quaternion dest)
    {
        BoneTarget target = targets[index];
        if (target.hasRotation)
        {
            dest.set(target.rotation);
            return true;
        }
        if (!base) return false;
        rotationSoFar(index, dest);
        return true;
    }

    /** As {@link #absoluteRotation}, for the slot's offset. */
    public boolean absoluteOffset(int index, Vec3f dest)
    {
        BoneTarget target = targets[index];
        if (target.hasOffset && !target.offsetAdditive)
        {
            dest.set(target.offset);
            return true;
        }
        if (!base) return false;
        offsetSoFar(index, dest);
        return true;
    }

    /** As {@link #absoluteRotation}, for the slot's scale. */
    public boolean absoluteScale(int index, Vec3f dest)
    {
        BoneTarget target = targets[index];
        if (target.hasScale && !target.scaleRelative)
        {
            dest.set(target.scale);
            return true;
        }
        if (!base) return false;
        scaleSoFar(index, dest);
        return true;
    }

    /** As {@link #absoluteRotation}, for the slot's vector. */
    public boolean absoluteVector(int index, Vec3f dest)
    {
        BoneTarget target = targets[index];
        if (target.hasVector && !target.vectorAdditive)
        {
            dest.set(target.vector);
            return true;
        }
        if (!base) return false;
        vectorSoFar(index, dest);
        return true;
    }

    /**
     * The rotation a slot is drawn at: its sink's, smoothed towards its targets (as of the last
     * frame), or, without a sink, what it has so far this frame.
     */
    public void rotationDrawn(int index, Quaternion dest)
    {
        IBoneSink sink = sink(index);
        IRotationSink rotationSink = sink == null ? null : sink.asRotation();
        if (rotationSink == null)
        {
            rotationSoFar(index, dest);
            return;
        }
        dest.set(rotationSink.getRotationShown());
    }

    /**
     * Composes a rotation into a slot. OVERRIDE replaces (folding away relative parts); PRE and
     * POST either fold into an existing absolute value, or accumulate as relative parts to be
     * applied to whatever lies beneath at composite time. A base pose resolves them against the
     * rest pose right away.
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
                else if (base)
                {
                    target.rotation.set(q);
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
                else if (base)
                {
                    target.rotation.set(q);
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

    /**
     * Writes a part's scale. OVERRIDE replaces it (an axis given as NaN keeps what the part has so
     * far this frame, as {@link #composeOffset}); PRE and POST multiply it, per axis (a scale has no
     * orientation of its own, so the two are the same).
     */
    public void composeScale(int index, float x, float y, float z, Space space)
    {
        BoneTarget target = targets[index];
        if (space == Space.OVERRIDE)
        {
            if ((Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z)) && scaleBeneath(index, offsetTemp))
            {
                if (Float.isNaN(x)) x = offsetTemp.x;
                if (Float.isNaN(y)) y = offsetTemp.y;
                if (Float.isNaN(z)) z = offsetTemp.z;
            }
            target.scale.set(x, y, z);
            target.scaleRelative = false;
        }
        else if (target.hasScale)
        {
            target.scale.scale(x, y, z);
        }
        else if (base)
        {
            target.scale.set(x, y, z);
            target.scaleRelative = false;
        }
        else
        {
            target.scale.set(x, y, z);
            target.scaleRelative = true;
        }
        target.hasScale = true;
    }

    /** What an OVERRIDE scale's unwritten axes keep (see {@link #composeScale}); false where it isn't known. */
    private boolean scaleBeneath(int index, Vec3f dest)
    {
        BoneTarget target = targets[index];
        if (target.hasScale && !target.scaleRelative)
        {
            dest.set(target.scale);
            return true;
        }
        boolean known = below != null ? below.absoluteScale(index, dest) : absoluteScale(index, dest);
        if (known && target.hasScale)
        {
            dest.scale(target.scale.x, target.scale.y, target.scale.z);
        }
        return known;
    }

    /**
     * Writes a part's offset. An OVERRIDE axis given as NaN isn't written: it keeps the value the bone
     * has so far this frame, from the items before and the layers below, or rest (a pose with nothing
     * beneath it, a mirrored item's, leaves it NaN to be filled when it is composited).
     */
    public void composeOffset(int index, float x, float y, float z, Space space)
    {
        BoneTarget target = targets[index];
        if (space == Space.OVERRIDE)
        {
            if ((Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z)) && offsetBeneath(index, offsetTemp))
            {
                if (Float.isNaN(x)) x = offsetTemp.x;
                if (Float.isNaN(y)) y = offsetTemp.y;
                if (Float.isNaN(z)) z = offsetTemp.z;
            }
            if (target.hasOffset && target.offsetMode == IVectorSink.Mode.SNAP)
            {
                // A snap followed by another write in the same frame: the new interpolation
                // starts from the snapped value.
                target.hasVectorStart = true;
                target.vectorStart.set(target.offset);
            }
            else if (target.hasOffset)
            {
                // Two writers with different targets in one frame: the slide restarts from the
                // current value.
                if (!Float.isNaN(x) && x != target.offset.x) target.restartX = true;
                if (!Float.isNaN(y) && y != target.offset.y) target.restartY = true;
                if (!Float.isNaN(z) && z != target.offset.z) target.restartZ = true;
            }
            target.offset.set(x, y, z);
            target.offsetAdditive = false;
        }
        else if (target.hasOffset)
        {
            target.offset.add(x, y, z);
        }
        else if (base)
        {
            target.offset.set(x, y, z);
            target.offsetAdditive = false;
        }
        else
        {
            target.offset.set(x, y, z);
            target.offsetAdditive = true;
        }
        target.hasOffset = true;
    }

    /** What an OVERRIDE offset's unwritten axes keep (see {@link #composeOffset}); false where it isn't known. */
    private boolean offsetBeneath(int index, Vec3f dest)
    {
        BoneTarget target = targets[index];
        if (target.hasOffset && !target.offsetAdditive)
        {
            dest.set(target.offset);
            return true;
        }
        boolean known = below != null ? below.absoluteOffset(index, dest) : absoluteOffset(index, dest);
        if (known && target.hasOffset)
        {
            dest.add(target.offset.x, target.offset.y, target.offset.z);
        }
        return known;
    }

    /** As {@link #composeOffset}, for a vector (an OVERRIDE axis given as NaN keeps what the bone has so far). */
    public void composeVector(int index, float x, float y, float z, Space space)
    {
        BoneTarget target = targets[index];
        if (space == Space.OVERRIDE)
        {
            if ((Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z)) && vectorBeneath(index, offsetTemp))
            {
                if (Float.isNaN(x)) x = offsetTemp.x;
                if (Float.isNaN(y)) y = offsetTemp.y;
                if (Float.isNaN(z)) z = offsetTemp.z;
            }
            if (target.hasVector && target.vectorMode == IVectorSink.Mode.SNAP)
            {
                // A snap followed by another write in the same frame: the new interpolation
                // starts from the snapped value.
                target.hasVectorStart = true;
                target.vectorStart.set(target.vector);
            }
            else if (target.hasVector)
            {
                // Two writers with different targets in one frame: the slide restarts from the
                // current value.
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
        else if (base)
        {
            target.vector.set(x, y, z);
            target.vectorAdditive = false;
        }
        else
        {
            target.vector.set(x, y, z);
            target.vectorAdditive = true;
        }
        target.hasVector = true;
    }

    /** What an OVERRIDE vector's unwritten axes keep (see {@link #composeVector}); false where it isn't known. */
    private boolean vectorBeneath(int index, Vec3f dest)
    {
        BoneTarget target = targets[index];
        if (target.hasVector && !target.vectorAdditive)
        {
            dest.set(target.vector);
            return true;
        }
        boolean known = below != null ? below.absoluteVector(index, dest) : absoluteVector(index, dest);
        if (known && target.hasVector)
        {
            dest.add(target.vector.x, target.vector.y, target.vector.z);
        }
        return known;
    }

    private IBoneSink sink(int index)
    {
        if (owner == null || index >= owner.sinks.length)
        {
            return null;
        }
        return owner.sinks[index];
    }

    private static boolean isFinite(Quaternion q)
    {
        return Float.isFinite(q.x) && Float.isFinite(q.y) && Float.isFinite(q.z) && Float.isFinite(q.w);
    }

    /** Puts every axis of {@code v} that isn't a finite number at {@code rest}. */
    private static void finiteOr(Vec3f v, float rest)
    {
        if (!Float.isFinite(v.x)) v.x = rest;
        if (!Float.isFinite(v.y)) v.y = rest;
        if (!Float.isFinite(v.z)) v.z = rest;
    }

    /**
     * Writes the pose into its entity's bones: every bone, every channel. What nothing wrote this
     * frame goes back to rest, at the bone's base smoothing, and so does a value that isn't a finite
     * number, so no bone can be made to vanish or keep a stale value. Only for a base pose, whose slots hold
     * no relative parts.
     */
    public void writeTo()
    {
        for (int i = 0; i < targets.length; i++)
        {
            BoneTarget target = targets[i];
            IBoneSink sink = sink(i);
            if (sink == null)
            {
                continue;
            }
            // What nothing wrote, and what isn't a finite number (a division by zero), is at rest.
            if (!target.hasRotation || !isFinite(target.rotation)) target.rotation.setIdentity();
            if (!target.hasOffset) target.offset.set(0F, 0F, 0F);
            if (!target.hasScale) target.scale.set(1F, 1F, 1F);
            if (!target.hasVector) target.vector.set(0F, 0F, 0F);
            finiteOr(target.offset, 0F);
            finiteOr(target.scale, 1F);
            finiteOr(target.vector, 0F);

            IRotationSink rotationSink = sink.asRotation();
            if (rotationSink != null)
            {
                rotationSink.setRotationTarget(target.rotation, target.smoothing, target.snap, target.hasSnapFrom ? target.snapFrom : null);
                if (rotationSink.hasOffset())
                {
                    rotationSink.setOffsetTarget(target.offset.x, target.offset.y, target.offset.z,
                            target.offsetSmoothing.x, target.offsetSmoothing.y, target.offsetSmoothing.z,
                            target.offsetMode, target.hasVectorStart ? target.vectorStart : null);
                    if (target.offsetMode == IVectorSink.Mode.SLIDE && (target.restartX || target.restartY || target.restartZ))
                    {
                        rotationSink.restartOffsetSlide(target.restartX, target.restartY, target.restartZ);
                    }
                }
                if (rotationSink.hasScale())
                {
                    rotationSink.setScaleTarget(target.scale.x, target.scale.y, target.scale.z);
                }
            }

            IVectorSink vectorSink = sink.asVector();
            if (vectorSink != null)
            {
                vectorSink.setVectorTarget(target.vector.x, target.vector.y, target.vector.z,
                        target.vectorSmoothing.x, target.vectorSmoothing.y, target.vectorSmoothing.z,
                        target.vectorMode, target.hasVectorStart ? target.vectorStart : null);
                if (target.vectorMode == IVectorSink.Mode.SLIDE && (target.restartX || target.restartY || target.restartZ))
                {
                    vectorSink.restartSlide(target.restartX, target.restartY, target.restartZ);
                }
            }
        }
    }

}
