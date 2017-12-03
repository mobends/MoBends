package net.gobbob.mobends.event;

import net.gobbob.mobends.AnimatedEntity;
import net.gobbob.mobends.client.renderer.entity.RenderBendsPlayer;
import net.gobbob.mobends.settings.SettingsNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventHandler_RenderPlayer {
	
	public static float partialTicks;
	
	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event){
		//System.out.println("Rendering player... ");
		partialTicks = event.renderTickTime;
	}
	
	@SubscribeEvent
	public void onPlayerRender(RenderLivingEvent.Pre event){
		
		if(!(event.entity instanceof EntityPlayer)){
			return;
		}
		
		if(event.renderer instanceof RenderBendsPlayer){
			return;
		}
		
		if(AnimatedEntity.getByEntity(event.entity).animate){
			AbstractClientPlayer player = (AbstractClientPlayer) event.entity;
			
			event.setCanceled(true);
			
			AnimatedEntity.getPlayerRenderer(player).doRender(player, event.x, event.y, event.z, 0.0f, partialTicks);
		}
		//System.out.println("Rendering player...");
	}
}
