package goblinbob.mobends.core.math.vector;

public class Vec3dReadonly implements IVec3dRead
{
	
	private final double x, y, z;
	
	public Vec3dReadonly(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3dReadonly(IVec3dRead other)
	{
		this.x = other.getX();
		this.y = other.getY();
		this.z = other.getZ();
	}
	
	public Vec3dReadonly()
	{
		this(0, 0, 0);
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
