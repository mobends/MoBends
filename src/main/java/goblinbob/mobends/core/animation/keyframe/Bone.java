package goblinbob.mobends.core.animation.keyframe;

import java.util.Arrays;

public class Bone
{
	private Keyframe[] keyframes;

	public Bone(Keyframe[] keyframes)
	{
		this.keyframes = keyframes;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Bone))
			return false;

		Bone other = (Bone) obj;

		return Arrays.equals(other.keyframes, this.keyframes);
	}

	public Keyframe[] getKeyframes()
	{
		return keyframes;
	}
}
