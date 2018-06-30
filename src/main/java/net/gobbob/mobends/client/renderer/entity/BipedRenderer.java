package net.gobbob.mobends.client.renderer.entity;

import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.main.ModConfig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;

public class BipedRenderer extends MutatedRenderer
{
	@Override
	public void renderLocalAccessories(EntityLivingBase entity, float partialTicks)
	{
		float scale = 0.0625F;
		
		EntityData data = EntityDatabase.instance.get(entity);
		if (data instanceof BipedEntityData)
		{
			BipedEntityData bipedData = (BipedEntityData) data;
			if (ModConfig.showSwordTrail)
			{
				GlStateManager.pushMatrix();
				GlStateManager.rotate(-this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks) + 180.0F, 0F, 1F, 0F);
				GlStateManager.scale(scale, scale, scale);
				bipedData.swordTrail.render();
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.popMatrix();
			}
		}
	}
}
