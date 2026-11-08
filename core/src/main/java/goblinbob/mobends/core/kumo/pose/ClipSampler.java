package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.animation.keyframe.Bone;
import goblinbob.mobends.core.animation.keyframe.Keyframe;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

/** Samples a keyframe clip at a fractional keyframe index. */
public class ClipSampler
{

    /** What a keyframe that leaves its rotation or position out has. */
    private static final float[] IDENTITY = {0, 0, 0, 1};
    private static final float[] ZERO = {0, 0, 0};

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
        final float[] ra = a.rotation != null ? a.rotation : IDENTITY;
        final float[] rb = b.rotation != null ? b.rotation : IDENTITY;
        final float[] pa = a.position != null ? a.position : ZERO;
        final float[] pb = b.position != null ? b.position : ZERO;

        // Hemisphere correction: neighbouring keyframes that straddle the +-180 degree wrap are
        // stored as q and ~-q; interpolating towards -b takes the short way round.
        float dot = ra[0] * rb[0] + ra[1] * rb[1] + ra[2] * rb[2] + ra[3] * rb[3];
        float sign = dot < 0 ? -1F : 1F;
        rotation.set(ra[0] + (sign * rb[0] - ra[0]) * tween,
                ra[1] + (sign * rb[1] - ra[1]) * tween,
                ra[2] + (sign * rb[2] - ra[2]) * tween,
                ra[3] + (sign * rb[3] - ra[3]) * tween);
        rotation.normalise();

        position.set(pa[0] + (pb[0] - pa[0]) * tween,
                pa[1] + (pb[1] - pa[1]) * tween,
                pa[2] + (pb[2] - pa[2]) * tween);
        return true;
    }

}
