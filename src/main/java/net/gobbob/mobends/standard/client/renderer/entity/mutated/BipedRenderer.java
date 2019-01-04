package net.gobbob.mobends.standard.client.renderer.entity.mutated;

import net.gobbob.mobends.core.client.MutatedRenderer;
import net.gobbob.mobends.core.data.EntityData;
import net.gobbob.mobends.standard.data.BipedEntityData;
import net.gobbob.mobends.standard.main.ModConfig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;

public class BipedRenderer<T extends EntityLivingBase> extends MutatedRenderer<T>
{
	@Override
	public void renderLocalAccessories(T entity, EntityData<?> data, float partialTicks)
	{
		float scale = 0.0625F;
		
		if (data instanceof BipedEntityData)
		{
			BipedEntityData<?> bipedData = (BipedEntityData<?>) data;
			if (ModConfig.showSwordTrail)
			{
				GlStateManager.pushMatrix();
				GlStateManager.scale(scale, scale, scale);
				bipedData.swordTrail.render();
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.popMatrix();
			}	
		}
	}
	
	@Override
	protected void transformLocally(T entity, EntityData<?> data, float partialTicks)
	{
		if (entity.isSneaking())
		{
			GlStateManager.translate(0F, 5F * scale, 0F);
		}
	}
}
