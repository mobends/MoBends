package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.animation.keyframe.Bone;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A clip bound to skeleton indices, plus the clips' naming rules: the "root" bone's position drives
 * the global offset, "centerRotation" only rotates the entity, and every other bone's position is
 * applied as a negated model offset.
 */
public class ClipBinding
{

    public static final String CENTER_ROTATION = "centerRotation";

    public final KeyframeAnimation animation;
    public final int keyframeCount;
    public final boolean step;

    final Bone[] bones;
    final int[] slots;
    final boolean[] isRoot;
    final boolean[] isCenterRotation;

    private final Quaternion rotation = new Quaternion();
    private final Quaternion scaled = new Quaternion();
    private final Vec3f position = new Vec3f();

    public ClipBinding(KeyframeAnimation animation, Skeleton skeleton, Collection<String> onlyBones)
    {
        this.animation = animation;
        this.keyframeCount = ClipSampler.keyframeCount(animation);
        this.step = "STEP".equalsIgnoreCase(animation.interpolation);

        List<Map.Entry<String, Bone>> entries = new ArrayList<>();
        for (Map.Entry<String, Bone> entry : animation.bones.entrySet())
        {
            if (onlyBones == null || onlyBones.contains(entry.getKey()))
            {
                entries.add(entry);
            }
        }

        bones = new Bone[entries.size()];
        slots = new int[entries.size()];
        isRoot = new boolean[entries.size()];
        isCenterRotation = new boolean[entries.size()];
        for (int i = 0; i < entries.size(); i++)
        {
            String name = entries.get(i).getKey();
            bones[i] = entries.get(i).getValue();
            slots[i] = skeleton.indexOf(name);
            isRoot[i] = Skeleton.isVectorBone(name);
            isCenterRotation[i] = CENTER_ROTATION.equals(name);
        }
    }

    /** The skeleton slots this clip writes (bones plus the root vector when a root bone exists). */
    public int[] writtenSlots()
    {
        return slots.clone();
    }

    /** Samples every bound bone at the index and composes it into the pose in one space. */
    public void apply(Pose pose, float index, float weight, Pose.Space space)
    {
        apply(pose, index, weight, space, null);
    }

    /**
     * @param spaces per-bone spaces (parallel to the clip's bone list), or null to use {@code space}.
     */
    public void apply(Pose pose, float index, float weight, Pose.Space space, Pose.Space[] spaces)
    {
        for (int i = 0; i < bones.length; i++)
        {
            if (!ClipSampler.sample(bones[i], index, step, rotation, position))
            {
                continue;
            }

            Pose.Space boneSpace = spaces != null ? spaces[i] : space;

            if (isRoot[i])
            {
                pose.composeVector(slots[i], position.x * weight, position.y * weight, position.z * weight, boneSpace);
                continue;
            }

            PoseMath.scale(rotation, weight, scaled);
            pose.composeRotation(slots[i], scaled, boneSpace);

            // centerRotation only rotates; its keyframe positions are ignored.
            if (!isCenterRotation[i])
            {
                pose.composeOffset(slots[i], -position.x * weight, -position.y * weight, -position.z * weight, boneSpace);
            }
        }
    }

    public int[] boneSlots()
    {
        return slots.clone();
    }

}
