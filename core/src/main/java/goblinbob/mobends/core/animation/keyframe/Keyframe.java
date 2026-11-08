package goblinbob.mobends.core.animation.keyframe;

import javax.annotation.Nullable;

public class Keyframe
{

	/**
	 * Each field may be left out of a clip file: a keyframe that leaves it out has none (no offset,
	 * no rotation). On the entity's own bones, a track whose keyframes all leave one out doesn't
	 * write it (see {@link Bone#keysPosition}).
	 */
	@Nullable
	public float[] position;
	/** A quaternion: X, Y, Z, W. */
	@Nullable
	public float[] rotation;

}
