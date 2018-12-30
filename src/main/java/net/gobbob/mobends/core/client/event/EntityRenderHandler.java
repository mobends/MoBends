package net.gobbob.mobends.core.client.event;

import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityRenderHandler
{
	@SubscribeEvent
	public void beforeLivingRender(RenderLivingEvent.Pre<? extends EntityLivingBase> event)
	{
		EntityLivingBase living = event.getEntity();
		AnimatedEntity<EntityLivingBase> animatedEntity = AnimatedEntityRegistry.getForEntity(living);
		if (animatedEntity == null)
			return;

		float pt = event.getPartialRenderTick();
		GlStateManager.pushMatrix();	
		if (animatedEntity.isAnimated())
		{
			if (animatedEntity.applyMutation(event.getRenderer(), living, pt))
			{
				animatedEntity.beforeRender(living, pt);
			}
		}
		else
		{
			animatedEntity.deapplyMutation(event.getRenderer(), living);
		}
	}

	@SubscribeEvent
	public void afterLivingRender(RenderLivingEvent.Post<? extends EntityLivingBase> event)
	{
		AnimatedEntity<EntityLivingBase> animatedEntity = AnimatedEntityRegistry.getForEntity(event.getEntity());
		if (animatedEntity == null)
			return;

		animatedEntity.afterRender(event.getEntity(), event.getPartialRenderTick());
		GlStateManager.popMatrix();
	}
}
