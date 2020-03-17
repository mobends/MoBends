package goblinbob.mobends.core.math.vector;

public class Vec4fReadOnly implements IVec4fRead
{
	
	private final float x, y, z, w;

	public Vec4fReadOnly(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Vec4fReadOnly(IVec4fRead other)
	{
		this.x = other.getX();
		this.y = other.getY();
		this.z = other.getZ();
		this.w = other.getW();
	}
	
	public Vec4fReadOnly()
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
	
}
