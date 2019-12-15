package net.gobbob.mobends.core.math.matrix;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class Mat4x4dBuffered extends Mat4x4d
{
	
	private final FloatBuffer buffer;
	
	public Mat4x4dBuffered()
	{
		super();
		this.buffer = BufferUtils.createFloatBuffer(16);
	}
	
	public void updateBuffer()
	{
		MatrixUtils.matToGlMatrix(this, this.buffer);
	}
	
	public FloatBuffer getBuffer()
	{
		return this.buffer;
	}
	
}
