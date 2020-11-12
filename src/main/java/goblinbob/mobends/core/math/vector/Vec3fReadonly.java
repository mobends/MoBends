package goblinbob.mobends.core.math.vector;

public class Vec3fReadonly implements IVec3fRead
{
	
	private final float x, y, z;
	
	public Vec3fReadonly(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3fReadonly(IVec3fRead other)
	{
		this.x = other.getX();
		this.y = other.getY();
		this.z = other.getZ();
	}
	
	public Vec3fReadonly()
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

	@Override
	public float lengthSq()
	{
		return x*x + y*y + z*z;
	}
	
}
