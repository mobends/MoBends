package goblinbob.mobends.core.util;

import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.QuaternionUtils;
import goblinbob.mobends.core.math.vector.IVec3fRead;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class GlHelper
{
	
	private static final FloatBuffer BUF_FLOAT_16 = BufferUtils.createFloatBuffer(16);
	
	public static void vertex(IVec3fRead vector)
	{
		GlStateManager.glVertex3f(vector.getX(), vector.getY(), vector.getZ());
	}
	
	public static void rotate(Quaternion quaternionIn)
    {
        GlStateManager.multMatrix(QuaternionUtils.quatToGlMatrix(BUF_FLOAT_16, quaternionIn));
    }
	
}
