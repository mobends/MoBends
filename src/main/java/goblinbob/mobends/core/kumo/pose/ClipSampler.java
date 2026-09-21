package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.animation.keyframe.Bone;
import goblinbob.mobends.core.animation.keyframe.Keyframe;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

/** Samples a keyframe clip at a fractional keyframe index. */
public class ClipSampler
{

    public static int keyframeCount(KeyframeAnimation animation)
    {
        int count = 0;
        for (Bone bone : animation.bones.values())
        {
            if (bone.keyframes.size() > count)
            {
                count = bone.keyframes.size();
            }
        }
        return count;
    }

    /**
     * @param index        fractional keyframe index, clamped to the bone's range.
     * @param step         true for stepped (hold) interpolation instead of linear.
     * @param rotation     receives the sampled rotation (normalised).
     * @param position     receives the sampled position.
     * @return false if the bone has no keyframes.
     */
    public static boolean sample(Bone bone, float index, boolean step, Quaternion rotation, Vec3f position)
    {
        final int count = bone.keyframes.size();
        if (count == 0)
        {
            return false;
        }

        if (index < 0) index = 0;
        int frameA = (int) index;
        if (frameA > count - 1) frameA = count - 1;
        int frameB = Math.min(frameA + 1, count - 1);
        float tween = step ? 0F : Math.min(index - frameA, 1F);

        Keyframe a = bone.keyframes.get(frameA);
        Keyframe b = bone.keyframes.get(frameB);

        rotation.set(a.rotation[0] + (b.rotation[0] - a.rotation[0]) * tween,
                a.rotation[1] + (b.rotation[1] - a.rotation[1]) * tween,
                a.rotation[2] + (b.rotation[2] - a.rotation[2]) * tween,
                a.rotation[3] + (b.rotation[3] - a.rotation[3]) * tween);
        rotation.normalise();

        position.set(a.position[0] + (b.position[0] - a.position[0]) * tween,
                a.position[1] + (b.position[1] - a.position[1]) * tween,
                a.position[2] + (b.position[2] - a.position[2]) * tween);
        return true;
    }

}
