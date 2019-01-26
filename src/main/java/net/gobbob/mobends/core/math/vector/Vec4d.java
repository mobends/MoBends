package net.gobbob.mobends.core.math.vector;

public class Vec4d implements IVec4d
{

	public double x, y, z, w;
	
	public Vec4d(double x, double y, double z, double w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Vec4d(IVec4dRead other)
	{
		this.x = other.getX();
		this.y = other.getY();
		this.z = other.getZ();
		this.w = other.getW();
	}
	
	public Vec4d()
	{
		this(0, 0, 0, 0);
	}
	
	@Override
	public double getX()
	{
		return x;
	}

	@Override
	public double getY()
	{
		return y;
	}

	@Override
	public double getZ()
	{
		return z;
	}

	@Override
	public double getW()
	{
		return w;
	}

	@Override
	public void set(double x, double y, double z, double w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void setX(double x)
	{
		this.x = x;
	}

	@Override
	public void setY(double y)
	{
		this.y = y;
	}

	@Override
	public void setZ(double z)
	{
		this.z = z;
	}

	@Override
	public void setW(double w)
	{
		this.w = w;
	}

	@Override
	public void add(double x, double y, double z, double w)
	{
		this.x += x;
		this.y += y;
		this.z += z;
		this.w += w;
	}
	
}
