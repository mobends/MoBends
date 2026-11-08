package goblinbob.mobends.core.animation.keyframe;

import java.util.List;

public class Bone
{

	public List<Keyframe> keyframes;

	/** Whether any of its keyframes gives a rotation: on the entity's own bones, a track that gives none leaves the rotation alone. */
	public boolean keysRotation()
	{
		for (Keyframe keyframe : keyframes)
		{
			if (keyframe.rotation != null) return true;
		}
		return false;
	}

	/** Whether any of its keyframes gives a position: on the entity's own bones, a track that gives none leaves the position alone. */
	public boolean keysPosition()
	{
		for (Keyframe keyframe : keyframes)
		{
			if (keyframe.position != null) return true;
		}
		return false;
	}

}
