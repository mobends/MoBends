package goblinbob.mobends.core.animation.keyframe;

import java.util.Map;

public class KeyframeAnimation
{

    public Map<String, Bone> bones;

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
