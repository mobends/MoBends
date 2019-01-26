package net.gobbob.mobends.core.math.vector;

public class Vec4f implements IVec4f
{
	
	public static final Vec4fReadOnly ZERO = new Vec4fReadOnly(0, 0, 0, 0);
	
	public float x, y, z, w;
	
	public Vec4f(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Vec4f(IVec4fRead other)
	{
		this.x = other.getX();
		this.y = other.getY();
		this.z = other.getZ();
		this.w = other.getW();
	}
	
	public Vec4f()
	{
		this(0, 0, 0, 0);
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

	@Override
	public float getW()
	{
		return w;
	}

	@Override
	public void set(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void setX(float x)
	{
		this.x = x;
	}

	@Override
	public void setY(float y)
	{
		this.y = y;
	}

	@Override
	public void setZ(float z)
	{
		this.z = z;
	}

	@Override
	public void setW(float w)
	{
		this.w = w;
	}

	@Override
	public void add(float x, float y, float z, float w)
	{
		this.x += x;
		this.y += y;
		this.z += z;
		this.w += w;
	}

}
