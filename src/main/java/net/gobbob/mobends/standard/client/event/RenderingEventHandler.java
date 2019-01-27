package net.gobbob.mobends.standard.client.event;

import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;
import net.gobbob.mobends.standard.mutators.PlayerMutator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderingEventHandler
{
	
	@SubscribeEvent
	public void beforeHandRender(RenderHandEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		Entity viewEntity = mc.getRenderViewEntity();
		if (!(viewEntity instanceof AbstractClientPlayer))
			return;
		
		AbstractClientPlayer player = (AbstractClientPlayer) viewEntity;
		AnimatedEntity<EntityLivingBase> animatedPlayer = AnimatedEntityRegistry.getForEntity(player);
		if (animatedPlayer == null || !animatedPlayer.isAnimated())
			return;
		
		RenderPlayer renderPlayer = (RenderPlayer) mc.getRenderManager().<AbstractClientPlayer>getEntityRenderObject(player);
		PlayerMutator mutator = (PlayerMutator) AnimatedEntity.getMutatorForRenderer(AbstractClientPlayer.class, renderPlayer);
		if (mutator != null)
			mutator.poseForFirstPersonView();
	}
}
