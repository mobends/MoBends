package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.animation.keyframe.Bone;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.state.template.SpaceTemplate;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A clip bound to skeleton indices, plus the clips' naming rules: the entity's own bones ("root",
 * "center") take their keyframe positions as their position vectors, and every other bone's
 * position is applied as a negated model offset.
 */
public class ClipBinding
{

    public final KeyframeAnimation animation;
    public final int keyframeCount;
    public final boolean step;

    final Bone[] bones;
    final String[] names;
    final int[] slots;
    final boolean[] isEntityBone;
    /**
     * Per bone: whether it writes a rotation, a position. A part's track writes both; one of the
     * entity's own bones writes only what its keyframes give (see {@link Bone#keysRotation}), so a
     * root track of root motion alone leaves the root's rotation to the rest of the animator. The
     * item's bones can pick one of them, too: {@code "root.rotation"}, {@code "root.position"}.
     */
    final boolean[] keysRotation;
    final boolean[] keysPosition;

    private final Quaternion rotation = new Quaternion();
    private final Quaternion scaled = new Quaternion();
    private final Vec3f position = new Vec3f();
    private final Quaternion beneathRotation = new Quaternion();
    private final Vec3f beneath = new Vec3f();

    public ClipBinding(KeyframeAnimation animation, Skeleton skeleton, Collection<String> onlyBones)
    {
        this.animation = animation;
        this.keyframeCount = ClipSampler.keyframeCount(animation);
        this.step = "step".equals(animation.interpolation);

        List<Map.Entry<String, Bone>> entries = new ArrayList<>();
        for (Map.Entry<String, Bone> entry : animation.bones.entrySet())
        {
            // A bone with no keyframes writes nothing, so the item's effects don't go to it either.
            List<?> keyframes = entry.getValue().keyframes;
            if (keyframes == null || keyframes.isEmpty())
            {
                continue;
            }
            if (selects(onlyBones, entry.getKey(), ROTATION) || selects(onlyBones, entry.getKey(), POSITION))
            {
                entries.add(entry);
            }
        }

        bones = new Bone[entries.size()];
        names = new String[entries.size()];
        slots = new int[entries.size()];
        isEntityBone = new boolean[entries.size()];
        keysRotation = new boolean[entries.size()];
        keysPosition = new boolean[entries.size()];
        for (int i = 0; i < entries.size(); i++)
        {
            String name = entries.get(i).getKey();
            names[i] = name;
            bones[i] = entries.get(i).getValue();
            slots[i] = skeleton.indexOf(name);
            isEntityBone[i] = Skeleton.isEntityBone(name);
            keysRotation[i] = (!isEntityBone[i] || bones[i].keysRotation()) && selects(onlyBones, name, ROTATION);
            keysPosition[i] = (!isEntityBone[i] || bones[i].keysPosition()) && selects(onlyBones, name, POSITION);
        }
    }

    /**
     * Applies the clip at {@code weight}. A bone written relatively (PRE / POST) gets the clip's
     * rotation and offset scaled by the weight; one it replaces (OVERRIDE) is blended by the weight
     * from what the bone has so far this frame (the rest pose if nothing wrote it) to the clip.
     *
     * @param spaces per-bone spaces, parallel to the clip's bone list (see {@link #spaces}).
     */
    public void apply(Pose pose, float index, float weight, Pose.Space[] spaces)
    {
        for (int i = 0; i < bones.length; i++)
        {
            if (!ClipSampler.sample(bones[i], index, step, rotation, position))
            {
                continue;
            }

            Pose.Space boneSpace = spaces[i];

            boolean blend = boneSpace == Pose.Space.OVERRIDE && weight != 1F;

            if (keysRotation[i])
            {
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
            }

            if (!keysPosition[i])
            {
                continue;
            }
            if (isEntityBone[i])
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
            }
            else if (blend)
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

    /** A suffix of a name in a clip item's {@code bones}: that channel of the bone alone. */
    public static final String ROTATION = ".rotation", POSITION = ".position";

    /** Whether a clip item that applies {@code onlyBones} (null: all) applies {@code channel} of {@code bone}. */
    private static boolean selects(Collection<String> onlyBones, String bone, String channel)
    {
        return onlyBones == null || onlyBones.contains(bone) || onlyBones.contains(bone + channel);
    }

    /** Tells the item's effects what it writes of the entity's own bones (see {@link ItemEffects#writes}). */
    public void describe(ItemEffects effects)
    {
        for (int i = 0; i < bones.length; i++)
        {
            effects.writes(slots[i], keysRotation[i], keysPosition[i]);
        }
    }

    private static float lerp(float from, float to, float t)
    {
        return from + (to - from) * t;
    }

    /** The space of each bone the clip writes, parallel to its bone list: an item's {@code @space}, OVERRIDE where it has none. */
    public Pose.Space[] spaces(SpaceTemplate space)
    {
        Pose.Space[] result = new Pose.Space[names.length];
        for (int i = 0; i < names.length; i++)
        {
            result[i] = space == null ? Pose.Space.OVERRIDE : space.forBone(names[i], Pose.Space.OVERRIDE);
        }
        return result;
    }

    /** The skeleton slot of each bone the clip writes, parallel to its bone list. */
    public int[] slots()
    {
        return slots.clone();
    }

}
