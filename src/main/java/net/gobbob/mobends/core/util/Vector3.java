package net.gobbob.mobends.core.util;

public class Vector3
{
	public static final Vector3 ZERO = new Vector3(0, 0, 0);
	
	public float x, y, z;
	
	public Vector3(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3()
	{
		this(0, 0, 0);
	}
	
	public void set(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void setX(float x)
	{
		this.x = x;
	}
	
	public void setY(float y)
	{
		this.y = y;
	}
	
	public void setZ(float z)
	{
		this.z = z;
	}
	
	public void set(Vector3 vector)
	{
		this.set(vector.x, vector.y, vector.z);
	}
	
	public Vector3 scale(float f)
	{
		this.x *= f;
		this.y *= f;
		this.z *= f;
		return this;
	}
	
	public Vector3 add(float x, float y, float z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public void add(Vector3 vector)
	{
		this.add(vector.x, vector.y, vector.z);
	}
	
	public static float dot(Vector3 left, Vector3 right)
	{
		return left.x * right.x + left.y * right.y + left.z * right.z;
	}
	
	public static Vector3 multiply(Vector3 left, Vector3 right, Vector3 dest)
	{
		dest.x = left.x * right.x;
		dest.y = left.y * right.y;
		dest.z = left.z * right.z;
		return dest;
	}
	
	public static Vector3 cross(
			final Vector3 left,
			final Vector3 right,
			Vector3 dest)
	{
		dest.set(
			left.y * right.z - left.z * right.y,
			right.x * left.z - right.z * left.x,
			left.x * right.y - left.y * right.x
		);

		return dest;
	}
	
	public static float distance(Vector3 left, Vector3 right)
	{
		float dx = left.x - right.x;
		float dy = left.y - right.y;
		float dz = left.z - right.z;
		return (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
	}
}
