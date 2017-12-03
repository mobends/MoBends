package net.gobbob.mobends.client.event;

import net.gobbob.mobends.client.renderer.ArrowTrail;
import net.gobbob.mobends.configuration.SettingsManager;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class DataUpdateHandler {
	public static float lastNotedRenderTick = 0.0f;
	public static float lastNotedClientTick = 0.0f;
	public static float partialTicks = 0.0f;
	public static float ticks = 0.0f;
	public static float ticksPerFrame = 0.0f;
	
	@SubscribeEvent
	public void updateAnimations(TickEvent.RenderTickEvent event){
		if(Minecraft.getMinecraft().world == null) return;
		if(Minecraft.getMinecraft().player == null) return;
		
		if(lastNotedRenderTick != Minecraft.getMinecraft().player.ticksExisted+event.renderTickTime){
			lastNotedRenderTick = Minecraft.getMinecraft().player.ticksExisted+event.renderTickTime;
			
			for(int i = 0; i < EntityData.databases.length; i++) {
				EntityData.databases[i].updateRender(event.renderTickTime);
			}
			
			partialTicks = event.renderTickTime;
			if(Minecraft.getMinecraft().player != null) {
				if(this.ticks > (Minecraft.getMinecraft().player.ticksExisted+event.renderTickTime)) {
					onTicksRestart();
					this.ticks = (Minecraft.getMinecraft().player.ticksExisted+event.renderTickTime);
				}
				
				this.ticksPerFrame = Math.max(0.0f, (Minecraft.getMinecraft().player.ticksExisted+event.renderTickTime)-this.ticks);
				this.ticks = (Minecraft.getMinecraft().player.ticksExisted+event.renderTickTime);
			}
			
			if(SettingsManager.ARROW_TRAILS.isEnabled())
				ArrowTrail.onRenderTick();
		}
	}
	
	public static void onTicksRestart() {
		
	}
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event){
		if(Minecraft.getMinecraft().world == null) return;
		
		if(lastNotedClientTick != Minecraft.getMinecraft().player.ticksExisted){
			lastNotedClientTick = Minecraft.getMinecraft().player.ticksExisted;
			
			for(int d = 0; d < EntityData.databases.length; d++) {
				EntityData.databases[d].updateClient();
			}
		}
	}
}
