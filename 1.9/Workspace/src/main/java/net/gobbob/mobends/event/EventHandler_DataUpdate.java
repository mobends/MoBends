package net.gobbob.mobends.event;

import java.util.Collection;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.client.renderer.entity.RenderBendsPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.Data_Skeleton;
import net.gobbob.mobends.data.Data_Spider;
import net.gobbob.mobends.data.Data_Zombie;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.util.BendsLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventHandler_DataUpdate {
	public static float lastNotedRenderTick = 0.0f;
	public static float lastNotedClientTick = 0.0f;
	
	@SubscribeEvent
	public void updateAnimations(TickEvent.RenderTickEvent event){
		if(Minecraft.getMinecraft().theWorld == null) return;
		if(Minecraft.getMinecraft().thePlayer == null) return;
		
		if(lastNotedRenderTick != Minecraft.getMinecraft().thePlayer.ticksExisted+event.renderTickTime){
			lastNotedRenderTick = Minecraft.getMinecraft().thePlayer.ticksExisted+event.renderTickTime;
			
			for(int i = 0; i < EntityData.databases.length; i++) {
				EntityData.databases[i].updateRender(event.renderTickTime);
			}
		}
	}
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event){
		if(Minecraft.getMinecraft().theWorld == null) return;
		
		if(lastNotedClientTick != Minecraft.getMinecraft().thePlayer.ticksExisted){
			lastNotedClientTick = Minecraft.getMinecraft().thePlayer.ticksExisted;
			
			for(int d = 0; d < EntityData.databases.length; d++) {
				EntityData.databases[d].updateClient();
			}
		}
	}
}
