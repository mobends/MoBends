package net.gobbob.mobends.client.event;

import java.util.Collection;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.MoBends;
import net.gobbob.mobends.client.renderer.ArrowTrail;
import net.gobbob.mobends.client.renderer.entity.RenderBendsPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.Data_Skeleton;
import net.gobbob.mobends.data.Data_Spider;
import net.gobbob.mobends.data.Data_Zombie;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.network.NetworkConfiguration;
import net.gobbob.mobends.settings.SettingManager;
import net.gobbob.mobends.util.BendsLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class DataUpdateHandler {
	public static float lastNotedRenderTick = 0.0f;
	public static float lastNotedClientTick = 0.0f;
	public static float partialTicks = 0.0f;
	public static float ticks = 0.0f;
	public static float ticksPerFrame = 0.0f;
	
	public static boolean renderingGuiScreen = false;
	
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
			
			if(SettingManager.ARROW_TRAILS.isEnabled())
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
			
			if(SettingManager.ARROW_TRAILS.isEnabled())
				ArrowTrail.cleanup();
		}
	}
	
	@SubscribeEvent
	public void onGuiScreenRender(GuiScreenEvent.DrawScreenEvent.Pre event) {
		renderingGuiScreen = true;
	}
	
	@SubscribeEvent
	public void afterGuiScreenRender(GuiScreenEvent.DrawScreenEvent.Post event) {
		renderingGuiScreen = false;
	}
}
