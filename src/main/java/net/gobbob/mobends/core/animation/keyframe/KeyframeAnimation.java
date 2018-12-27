package net.gobbob.mobends.core.animation.keyframe;

import java.util.List;

public class KeyframeAnimation
{
	public List<KeyframeArmature> armatures;

	public void mirrorRotationYZ(String boneName)
	{
		for (KeyframeArmature armature : armatures)
		{
			Bone bone = armature.bones.get(boneName);
			if (bone != null)
			{
				for (Keyframe keyframe : bone.keyframes)
				{
					keyframe.mirrorRotationYZ();
				}
			}
		}
	}
}
