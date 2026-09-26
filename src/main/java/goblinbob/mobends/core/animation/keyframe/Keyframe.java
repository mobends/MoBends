package goblinbob.mobends.core.animation.keyframe;

public class Keyframe
{
	/** Each field may be left out of a clip file; it then keeps its default (no offset, no rotation, unit scale). */
	public float[] position = { 0, 0, 0 };
	// X, Y, Z, W
	public float[] rotation = { 0, 0, 0, 1 };

	public float[] scale = { 1, 1, 1 };
	
	public void mirrorRotationYZ()
	{
		rotation[1] *= -1;
		rotation[2] *= -1;
	}

	public void swapRotationYZ()
	{
		float y = rotation[1];
		rotation[1] = rotation[2];
		rotation[2] = y;
	}

}
