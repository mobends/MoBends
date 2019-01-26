package net.gobbob.mobends.core.math.vector;

public class Vec3d implements IVec3d
{
	
	public static final Vec3dReadonly ZERO = new Vec3dReadonly(0, 0, 0);
	public static final Vec3dReadonly ONE = new Vec3dReadonly(1, 1, 1);
	
	public double x, y, z;
	
	public Vec3d(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3d(IVec3fRead other)
	{
		this.x = other.getX();
		this.y = other.getY();
		this.z = other.getZ();
	}
	
	public Vec3d()
	{
		this(0, 0, 0);
	}
	
	@Override
	public void set(double x, double y, double z)
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
	public void add(double x, double y, double z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
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
	public double lengthSq()
	{
		return x*x + y*y + z*z;
	}
	
}
