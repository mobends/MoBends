package net.gobbob.mobends.core.util;

import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import net.minecraft.client.renderer.GlStateManager;

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
		dest.set(quat);
		dest.rotate(x, y, z, angle / 180.0F * PI);
		return dest;
	}
	
	public static Quaternion rotate(final Quaternion quat, float angle, float x, float y, float z)
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
