package net.gobbob.mobends.core.util;

public class VectorUtils
{
	public static float dot(IVec3fRead left, IVec3fRead right)
	{
		return left.getX() * right.getX() + left.getY() * right.getY() + left.getZ() * right.getZ();
	}
	
	public static IVec3f multiply(IVec3fRead left, IVec3fRead right, IVec3f dest)
	{
		dest.setX(left.getX() * right.getX());
		dest.setY(left.getY() * right.getY());
		dest.setZ(left.getZ() * right.getZ());
		return dest;
	}
	
	public static IVec3fRead cross(
		IVec3fRead left,
		IVec3fRead right,
		IVec3f dest)
	{
		dest.set(
			left.getY()  * right.getZ() - left.getZ()  * right.getY(),
			right.getX() * left.getZ()  - right.getZ() * left.getX(),
			left.getX()  * right.getY() - left.getY()  * right.getX()
		);

		return dest;
	}
	
	public static float distance(IVec3fRead left, IVec3fRead right)
	{
		float dx = left.getX() - right.getX();
		float dy = left.getY() - right.getY();
		float dz = left.getZ() - right.getZ();
		return (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
	}

	public static void subtract(IVec3fRead a, IVec3fRead b, IVec3f dest)
	{
		dest.set(
			a.getX() - b.getX(),
			a.getY() - b.getY(),
			a.getZ() - b.getZ()
		);
	}
	
	public Vec3f subtract(IVec3fRead a, IVec3fRead b)
	{
		return new Vec3f(
			a.getX() - b.getX(),
			a.getY() - b.getY(),
			a.getZ() - b.getZ()
		);
	}
}
