package net.gobbob.mobends.standard.client.event;

import net.gobbob.mobends.core.EntityData;
import net.gobbob.mobends.core.EntityDatabase;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.standard.client.mutators.PlayerMutator;
import net.gobbob.mobends.standard.data.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderingEventHandler
{
	@SubscribeEvent
	public void beforeHandRender(RenderHandEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		Entity viewEntity = mc.getRenderViewEntity();
		AnimatedEntity animatedEntity = AnimatedEntity.getForEntity(viewEntity);
		EntityData entityData = EntityDatabase.instance.get(viewEntity);
		
		if (animatedEntity != null && animatedEntity.isAnimated() && entityData instanceof PlayerData)
		{
			Render<Entity> render = mc.getRenderManager().getEntityRenderObject(viewEntity);
			if (render instanceof RenderLivingBase)
			{
				PlayerMutator mutator = (PlayerMutator) AnimatedEntity.getMutatorForRenderer(AbstractClientPlayer.class, (RenderLivingBase) render);
				if (mutator != null)
					mutator.poseForFirstPersonView();
			}
		}
	}
}
