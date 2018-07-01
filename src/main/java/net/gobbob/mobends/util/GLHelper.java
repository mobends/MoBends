package net.gobbob.mobends.util;

import net.minecraft.client.renderer.GlStateManager;

public class GLHelper
{
	public static void vertex(final Vector3 vector)
	{
		GlStateManager.glVertex3f(vector.x, vector.y, vector.z);
	}
}
