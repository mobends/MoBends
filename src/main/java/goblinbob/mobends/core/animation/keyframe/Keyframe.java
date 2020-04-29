package goblinbob.mobends.core.animation.keyframe;

public class Keyframe
{
	public float[] position;
	// X, Y, Z, W
	public float[] rotation;

	public float[] scale;
	
	public void mirrorRotationYZ()
	{
		rotation[1] *= -1;
		rotation[2] *= -1;
	}
}
