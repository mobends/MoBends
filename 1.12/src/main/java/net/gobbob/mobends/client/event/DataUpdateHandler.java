package net.gobbob.mobends.client.event;

import net.gobbob.mobends.client.renderer.ArrowTrail;
import net.gobbob.mobends.configuration.SettingsManager;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class DataUpdateHandler {
	public static float lastNotedClientTick = 0.0f;
	public static float partialTicks = 0.0f;
	protected static float ticks = 0.0f;
	public static float ticksPerFrame = 0.0f;
	
	public static float getTicks() {
		return ticks;
	}
	
	@SubscribeEvent
	public void updateAnimations(TickEvent.RenderTickEvent event){
		if(Minecraft.getMinecraft().world == null) return;
		if(Minecraft.getMinecraft().player == null) return;
		
		if(ticks != Minecraft.getMinecraft().player.ticksExisted + event.renderTickTime){
			EntityDatabase.instance.updateRender(event.renderTickTime);
			
			partialTicks = event.renderTickTime;
			if(this.ticks > (Minecraft.getMinecraft().player.ticksExisted+event.renderTickTime)) {
				onTicksRestart();
				this.ticks = (Minecraft.getMinecraft().player.ticksExisted+event.renderTickTime);
			}
			
			this.ticksPerFrame = Math.max(0F, (Minecraft.getMinecraft().player.ticksExisted + event.renderTickTime) - ticks);
			ticks = (Minecraft.getMinecraft().player.ticksExisted + event.renderTickTime);
			
			if(SettingsManager.ARROW_TRAILS.isEnabled())
				ArrowTrail.onRenderTick();
		}
	}
	
	public static void onTicksRestart() {
		
	}
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event){
		if(Minecraft.getMinecraft().player == null) return;
		
		if(lastNotedClientTick != Minecraft.getMinecraft().player.ticksExisted){
			lastNotedClientTick = Minecraft.getMinecraft().player.ticksExisted;
			
			EntityDatabase.instance.updateClient();
		}
	}
}
