package goblinbob.mobends.core.math;

public class Quaternion implements IQuaternion
{
	public static final IQuaternionRead IDENTITY = new Quaternion(0, 0, 0, 1);

	public float x, y, z, w;
	
	public Quaternion(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Quaternion(IQuaternionRead src)
	{
		this.set(src);
	}

	@Override
	public void set(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
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
	public void set(IQuaternionRead quat)
	{
		this.x = quat.getX();
		this.y = quat.getY();
		this.z = quat.getZ();
		this.w = quat.getW();
	}

	@Override
	public void add(float x, float y, float z, float w)
	{
		this.x += x;
		this.y += y;
		this.z += z;
		this.w += w;
	}
	
	public void setIdentity()
	{
		this.set(0, 0, 0, 1);
	}
	
	public void normalise()
	{
		float length = this.length();
		
		if (length != 0)
		{
			float inv_l = 1F / length;
			this.x *= inv_l;
			this.y *= inv_l;
			this.z *= inv_l;
			this.w *= inv_l;
		}
	}
	
	public void negate()
	{
		this.x = -this.x;
		this.y = -this.y;
		this.z = -this.z;
	}

	public void setFromAxisAngle(float x, float y, float z, float angle)
	{
		float n = (float) Math.sqrt(x * x + y * y + z * z);
		// zero-div may occur.
		float s = (float) (Math.sin(0.5 * angle) / n);
		this.x = x * s;
		this.y = y * s;
		this.z = z * s;
		w = (float) Math.cos(0.5 * angle);
	}
	
	public void rotate(float x, float y, float z, float angle)
	{
		float n = (float) Math.sqrt(x * x + y * y + z * z);
		// zero-div may occur.
		float s = (float) (Math.sin(0.5 * angle) / n);
		float x2 = x * s;
		float y2 = y * s;
		float z2 = z * s;
		float w2 = (float) Math.cos(0.5 * angle);
		
		mul(x2, y2, z2, w2, this.x, this.y, this.z, this.w, this);
	}
	
	public static IQuaternion mul(IQuaternionRead left, IQuaternionRead right, IQuaternion dest)
	{
		dest.set(left.getX() * right.getW() + left.getW() * right.getX() + left.getY() * right.getZ()
				- left.getZ() * right.getY(), left.getY() * right.getW() + left.getW() * right.getY()
				+ left.getZ() * right.getX() - left.getX() * right.getZ(), left.getZ() * right.getW()
				+ left.getW() * right.getZ() + left.getX() * right.getY() - left.getY() * right.getX(),
				left.getW() * right.getW() - left.getX() * right.getX() - left.getY() * right.getY()
				- left.getZ() * right.getZ());
		return dest;
	}
	
	public static IQuaternion mul(float x1, float y1, float z1, float w1, float x2, float y2, float z2, float w2, IQuaternion dest)
	{
		dest.set(x1 * w2 + w1 * x2 + y1 * z2
				- z1 * y2, y1 * w2 + w1 * y2
				+ z1 * x2 - x1 * z2, z1 * w2
				+ w1 * z2 + x1 * y2 - y1 * x2,
				w1 * w2 - x1 * x2 - y1 * y2
				- z1 * z2);
		return dest;
	}
}
