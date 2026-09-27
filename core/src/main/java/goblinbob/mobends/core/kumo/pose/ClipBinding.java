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
    private final Quaternion beneathRotation = new Quaternion();
    private final Vec3f beneath = new Vec3f();

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
            isCenterRotation[i] = Skeleton.CENTER_ROTATION.equals(name);
        }
    }

    /**
     * Applies the clip at {@code weight}. A bone written relatively (PRE / POST) gets the clip's
     * rotation and offset scaled by the weight; one it replaces (OVERRIDE) is blended by the weight
     * from what the bone has so far this frame (the rest pose if nothing wrote it) to the clip.
     *
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

            boolean blend = boneSpace == Pose.Space.OVERRIDE && weight != 1F;

            if (isRoot[i])
            {
                if (blend)
                {
                    pose.vectorSoFar(slots[i], beneath);
                    pose.composeVector(slots[i], lerp(beneath.x, position.x, weight), lerp(beneath.y, position.y, weight), lerp(beneath.z, position.z, weight), boneSpace);
                }
                else
                {
                    pose.composeVector(slots[i], position.x * weight, position.y * weight, position.z * weight, boneSpace);
                }
                continue;
            }

            if (blend)
            {
                pose.rotationSoFar(slots[i], beneathRotation);
                PoseMath.slerp(beneathRotation, rotation, weight, scaled);
            }
            else
            {
                PoseMath.scale(rotation, weight, scaled);
            }
            pose.composeRotation(slots[i], scaled, boneSpace);

            // centerRotation only rotates; its keyframe positions are ignored.
            if (!isCenterRotation[i])
            {
                if (blend)
                {
                    pose.offsetSoFar(slots[i], beneath);
                    pose.composeOffset(slots[i], lerp(beneath.x, -position.x, weight), lerp(beneath.y, -position.y, weight), lerp(beneath.z, -position.z, weight), boneSpace);
                }
                else
                {
                    pose.composeOffset(slots[i], -position.x * weight, -position.y * weight, -position.z * weight, boneSpace);
                }
            }
        }
    }

    private static float lerp(float from, float to, float t)
    {
        return from + (to - from) * t;
    }

    /** The skeleton slot of each bone the clip writes, parallel to its bone list. */
    public int[] slots()
    {
        return slots.clone();
    }

}
