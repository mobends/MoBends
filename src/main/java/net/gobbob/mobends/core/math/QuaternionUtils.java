package net.gobbob.mobends.core.math;

import java.nio.FloatBuffer;

import net.gobbob.mobends.core.math.vector.IVec3f;
import net.gobbob.mobends.core.math.vector.Vec3f;
import net.gobbob.mobends.core.math.vector.VectorUtils;

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

    public static FloatBuffer quatToGlMatrix(FloatBuffer buffer, Quaternion quaternionIn)
    {
        buffer.clear();
        float f = quaternionIn.x * quaternionIn.x;
        float f1 = quaternionIn.x * quaternionIn.y;
        float f2 = quaternionIn.x * quaternionIn.z;
        float f3 = quaternionIn.x * quaternionIn.w;
        float f4 = quaternionIn.y * quaternionIn.y;
        float f5 = quaternionIn.y * quaternionIn.z;
        float f6 = quaternionIn.y * quaternionIn.w;
        float f7 = quaternionIn.z * quaternionIn.z;
        float f8 = quaternionIn.z * quaternionIn.w;
        buffer.put(1.0F - 2.0F * (f4 + f7));
        buffer.put(2.0F * (f1 + f8));
        buffer.put(2.0F * (f2 - f6));
        buffer.put(0.0F);
        buffer.put(2.0F * (f1 - f8));
        buffer.put(1.0F - 2.0F * (f + f7));
        buffer.put(2.0F * (f5 + f3));
        buffer.put(0.0F);
        buffer.put(2.0F * (f2 + f6));
        buffer.put(2.0F * (f5 - f3));
        buffer.put(1.0F - 2.0F * (f + f4));
        buffer.put(0.0F);
        buffer.put(0.0F);
        buffer.put(0.0F);
        buffer.put(0.0F);
        buffer.put(1.0F);
        buffer.rewind();
        return buffer;
    }
}
