package net.gobbob.mobends.animation.keyframe;

public class Keyframe
{
	public float[] position;
	// X, Y, Z, W
	public float[] rotation;
	
	public void mirrorRotationYZ()
	{
		rotation[1] *= -1;
		rotation[2] *= -1;
	}
}
