package goblinbob.mobends.core.math.vector;

public class Vec3f implements IVec3f
{
	
	public static final Vec3fReadonly ZERO = new Vec3fReadonly(0, 0, 0);
	
	public float x, y, z;
	
	public Vec3f(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3f(IVec3fRead other)
	{
		this.x = other.getX();
		this.y = other.getY();
		this.z = other.getZ();
	}
	
	public Vec3f()
	{
		this(0, 0, 0);
	}
	
	@Override
	public void set(float x, float y, float z)
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
	public void add(float x, float y, float z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
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
