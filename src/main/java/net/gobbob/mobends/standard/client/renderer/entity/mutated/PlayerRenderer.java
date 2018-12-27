package net.gobbob.mobends.client.renderer.entity.mutated;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;

public class PlayerRenderer extends BipedRenderer<AbstractClientPlayer>
{
	@Override
	protected void transformLocally(AbstractClientPlayer entity, float partialTicks)
	{
		if (entity.isSneaking())
		{
			if (entity.capabilities.isFlying)
			{
				GlStateManager.translate(0F, 4F * scale, 0F);
			}
			else
			{
				GlStateManager.translate(0F, 5F * scale, 0F);
			}
		}
	}
}
