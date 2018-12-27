package net.gobbob.mobends.core.client;

import net.gobbob.mobends.core.EntityData;
import net.gobbob.mobends.core.EntityDatabase;
import net.gobbob.mobends.core.LivingEntityData;
import net.gobbob.mobends.core.main.ModConfig;
import net.gobbob.mobends.core.util.GLHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public abstract class MutatedRenderer<T extends EntityLivingBase>
{
	protected final float scale = 0.0625F;
	
	/*
	 * Called right before the entity is rendered
	 */
	public void beforeRender(T entity, float partialTicks)
	{
		double entityX = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
		double entityY = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks;
		double entityZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
		
		Entity viewEntity = Minecraft.getMinecraft().getRenderViewEntity();
		double viewX = entityX, viewY = entityY, viewZ = entityZ;
		if (viewEntity != null)
		{
			// Checking in case of Main Menu or GUI rendering.
			viewX = viewEntity.prevPosX + (viewEntity.posX - viewEntity.prevPosX) * partialTicks;
			viewY = viewEntity.prevPosY + (viewEntity.posY - viewEntity.prevPosY) * partialTicks;
			viewZ = viewEntity.prevPosZ + (viewEntity.posZ - viewEntity.prevPosZ) * partialTicks;
		}
		GlStateManager.translate(entityX - viewX, entityY - viewY, entityZ - viewZ);
		GlStateManager.rotate(-this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks), 0F, 1F, 0F);
		
		this.renderLocalAccessories(entity, partialTicks);
		
		EntityData data = EntityDatabase.instance.get(entity);
		if (data != null && data instanceof LivingEntityData)
		{
			LivingEntityData livingData = (LivingEntityData) data;

			GlStateManager.translate(livingData.renderOffset.getX() * scale,
									 livingData.renderOffset.getY() * scale,
									 livingData.renderOffset.getZ() * scale);
			GlStateManager.translate(0, entity.height / 2, 0);
			GLHelper.rotate(livingData.centerRotation.getSmooth());
			GlStateManager.translate(0, -entity.height / 2, 0);
			GLHelper.rotate(livingData.renderRotation.getSmooth());
		}
		
		this.transformLocally(entity, partialTicks);
		
		GlStateManager.rotate(this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks), 0F, 1F, 0F);
		GlStateManager.translate(viewX - entityX, viewY - entityY, viewZ - entityZ);
	}
	
	/*
	 * Called right after the entity is rendered.
	 */
	public void afterRender(T entity, float partialTicks) {}
	
	/*
	 * Used to render accessories for that entity, e.g. Sword trails.
	 * Also used to transform the entity, like offset or rotate it.
	 */
	protected void renderLocalAccessories(T entity, float partialTicks) {}
	protected void transformLocally(T entity, float partialTicks) {}
	
	protected static float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks)
    {
        float f;
        for (f = yawOffset - prevYawOffset; f < -180.0F; f += 360.0F);

        while (f >= 180.0F)
            f -= 360.0F;

        return prevYawOffset + partialTicks * f;
    }
}
