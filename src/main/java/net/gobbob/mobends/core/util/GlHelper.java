package net.gobbob.mobends.core.util;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.core.math.Quaternion;
import net.gobbob.mobends.core.math.QuaternionUtils;
import net.gobbob.mobends.core.math.matrix.IMat4x4d;
import net.gobbob.mobends.core.math.matrix.MatrixUtils;
import net.gobbob.mobends.core.math.vector.IVec3dRead;
import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.minecraft.client.renderer.GlStateManager;

public class GlHelper
{
	
	private static final FloatBuffer BUF_FLOAT_16 = BufferUtils.createFloatBuffer(16);
	
	public static void vertex(IVec3fRead vector)
	{
		GlStateManager.glVertex3f(vector.getX(), vector.getY(), vector.getZ());
	}
	
	public static void vertex(IVec3dRead vector)
	{
		GL11.glVertex3d(vector.getX(), vector.getY(), vector.getZ());
	}
	
	public static void rotate(Quaternion quaternionIn)
    {
        GlStateManager.multMatrix(QuaternionUtils.quatToGlMatrix(BUF_FLOAT_16, quaternionIn));
    }
	
	public static void transform(IMat4x4d matrixIn)
	{
		GlStateManager.multMatrix(MatrixUtils.matToGlMatrix(matrixIn, BUF_FLOAT_16));
	}
	
}
