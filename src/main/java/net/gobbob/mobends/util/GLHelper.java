package net.gobbob.mobends.util;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import net.minecraft.client.renderer.GlStateManager;

public class GLHelper
{
	private static final FloatBuffer BUF_FLOAT_16 = BufferUtils.createFloatBuffer(16);
	
	public static void vertex(final Vector3 vector)
	{
		GlStateManager.glVertex3f(vector.x, vector.y, vector.z);
	}
	
	public static void rotate(Quaternion quaternionIn)
    {
        GlStateManager.multMatrix(QuaternionUtils.quatToGlMatrix(BUF_FLOAT_16, quaternionIn));
    }
}
