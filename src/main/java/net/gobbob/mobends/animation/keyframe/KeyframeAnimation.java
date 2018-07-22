package net.gobbob.mobends.animation.keyframe;

import java.util.List;
import java.util.Map;

public class KeyframeAnimation
{
	public List<KeyframeArmature> armatures;

	public void mirrorRotationYZ(String boneName)
	{
		for (KeyframeArmature armature : armatures)
		{
			for (Map.Entry<String, Bone> entry : armature.bones.entrySet())
			{
				if (entry.getKey().equals(boneName))
				{
					for (Keyframe keyframe : entry.getValue().keyframes)
					{
						keyframe.mirrorRotationYZ();
					}
				}
			}
		}
	}
}
