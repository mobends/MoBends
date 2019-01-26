package net.gobbob.mobends.core.util;

public class Vec3fReadOnly implements IVec3fRead
{
	private final float x, y, z;
	
	public Vec3fReadOnly(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3fReadOnly(Vec3fReadOnly other)
	{
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}
	
	public Vec3fReadOnly()
	{
		this(0, 0, 0);
	}

	@Override
	public float getX()
	{
		return x;
	}

	@Override
	public float getY()
	{
		return y;
	}

	@Override
	public float getZ()
	{
		return z;
	}
}
