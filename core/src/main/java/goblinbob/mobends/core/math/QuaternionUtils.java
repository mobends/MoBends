package goblinbob.mobends.core.math;

import goblinbob.mobends.core.math.vector.IVec3f;
import goblinbob.mobends.core.math.vector.Vec3f;
import goblinbob.mobends.core.math.vector.VectorUtils;

public class QuaternionUtils
{
	
	public static final float PI = (float) Math.PI;
	
	public static void multiply(IVec3f vector, Quaternion quat, IVec3f dest)
	{
		// Extract the vector part of the quaternion
		Vec3f u = new Vec3f(-quat.x, quat.y, quat.z);
		Vec3f crossResult = new Vec3f();
		
	    // Extract the scalar part of the quaternion
		final float s = -quat.w;

	    // Do the math
	    /*dest = 2.0f * dot(u, v) * u
	          + (s*s - dot(u, u)) * v
	          + 2.0f * s * cross(u, v);*/
	    final float x = vector.getX();
	    final float y = vector.getY();
	    final float z = vector.getZ();
	    
	    final float dotUU = VectorUtils.dot(u, u);
	    final float dotUV = VectorUtils.dot(u, vector);
	    VectorUtils.cross(u, vector, crossResult);
	    
	    dest.set(u);
	    dest.scale(2F * dotUV);
	    dest.add(x * (s*s - dotUU), y * (s*s - dotUU), z * (s*s - dotUU));
	    crossResult.scale(2 * s);
	    dest.add(crossResult);
	}
	
	public static Quaternion rotate(Quaternion quat, float angle, float x, float y, float z, Quaternion dest)
	{
		dest.set(quat);
		dest.rotate(x, y, z, angle / 180.0F * PI);
		return dest;
	}
	
	public static Quaternion rotate(Quaternion quat, float angle, float x, float y, float z)
	{
		quat.rotate(x, y, z, angle / 180.0F * PI);
		return quat;
	}

}
