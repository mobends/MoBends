package goblinbob.mobends.core.animation.keyframe;

public class Keyframe
{

	/** Each field may be left out of a clip file; it then keeps its default (no offset, no rotation). */
	public float[] position = { 0, 0, 0 };
	/** A quaternion: X, Y, Z, W. */
	public float[] rotation = { 0, 0, 0, 1 };

}
