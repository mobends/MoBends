package goblinbob.mobends.core.animation.keyframe;

import java.util.Map;

public class KeyframeAnimation
{

    public Map<String, Bone> bones;

    /**
     * Format 2 metadata (optional). Keyframes are evenly spaced over {@code duration} time
     * units (ticks, or whatever the playing node's time source produces); for a looping clip the
     * last keyframe coincides with the first.
     */
    public Float duration;
    public Boolean loop;
    /** "LINEAR" (default) or "STEP" (hold each keyframe until the next). */
    public String interpolation;

    public void mirrorRotationYZ(String boneName)
    {
        Bone bone = bones.get(boneName);
        if (bone != null)
            for (Keyframe keyframe : bone.keyframes)
                keyframe.mirrorRotationYZ();
    }

    public void swapRotationYZ(String boneName)
    {
        Bone bone = bones.get(boneName);
        if (bone != null)
            for (Keyframe keyframe : bone.keyframes)
                keyframe.swapRotationYZ();
    }

}
