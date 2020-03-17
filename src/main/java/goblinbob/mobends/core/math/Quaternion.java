package goblinbob.mobends.core.math;

public class Quaternion
{
	public float x, y, z, w;
	
	public Quaternion(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Quaternion()
	{
		this(0, 0, 0, 1);
	}
	
	public float lengthSquared()
	{
		return x * x + y * y + z * z + w * w;
	}
	
	public float length()
	{
		return (float) Math.sqrt(this.lengthSquared());
	}
	
	public void set(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public void set(Quaternion quat)
	{
		this.x = quat.x;
		this.y = quat.y;
		this.z = quat.z;
		this.w = quat.w;
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
	
	public static Quaternion mul(Quaternion left, Quaternion right, Quaternion dest)
	{
		dest.set(left.x * right.w + left.w * right.x + left.y * right.z
				- left.z * right.y, left.y * right.w + left.w * right.y
				+ left.z * right.x - left.x * right.z, left.z * right.w
				+ left.w * right.z + left.x * right.y - left.y * right.x,
				left.w * right.w - left.x * right.x - left.y * right.y
				- left.z * right.z);
		return dest;
	}
	
	public static Quaternion mul(float x1, float y1, float z1, float w1, float x2, float y2, float z2, float w2, Quaternion dest)
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
