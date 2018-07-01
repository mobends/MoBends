package net.gobbob.mobends.util;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class QuaternionUtils
{
	public static final float PI = (float) Math.PI;
	
	public static void multiply(final Vector3 vector, Quaternion quat, Vector3 dest)
	{
		// Extract the vector part of the quaternion
		Vector3 u = new Vector3(-quat.x, quat.y, quat.z);
		Vector3 crossResult = new Vector3();
		
	    // Extract the scalar part of the quaternion
	    float s = -quat.w;

	    // Do the math
	    /*dest = 2.0f * dot(u, v) * u
	          + (s*s - dot(u, u)) * v
	          + 2.0f * s * cross(u, v);*/
	    float x = vector.x;
	    float y = vector.y;
	    float z = vector.z;
	    
	    float dotUU = Vector3.dot(u, u);
	    float dotUV = Vector3.dot(u, vector);
	    Vector3.cross(u, vector, crossResult);
	    
	    dest.set(u);
	    dest.scale(2F * dotUV);
	    dest.add(x * (s*s - dotUU), y * (s*s - dotUU), z * (s*s - dotUU));
	    crossResult.scale(2 * s);
	    dest.add(crossResult);
	}
	
	public static Quaternion rotate(final Quaternion quat, float angle, float x, float y, float z, Quaternion dest)
	{
		Quaternion rotation = new Quaternion();
		rotation.setFromAxisAngle(new Vector4f(x, y, z, angle / 180.0F * PI));
		Quaternion.mul(rotation, quat, dest);
		return dest;
	}
	
	public static Quaternion rotate(final Quaternion quat, float angle, float x, float y, float z)
	{
		return rotate(quat, angle, x, y, z, quat);
	}
}
