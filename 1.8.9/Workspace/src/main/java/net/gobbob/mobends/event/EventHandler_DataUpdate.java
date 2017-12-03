package net.gobbob.mobends.event;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.Data_Spider;
import net.gobbob.mobends.data.Data_Zombie;
import net.gobbob.mobends.util.BendsLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventHandler_DataUpdate {
	
	@SubscribeEvent
	public void updateAnimations(TickEvent.RenderTickEvent event){
		if(Minecraft.getMinecraft().theWorld == null){
			return;
		}
		
		for(int i = 0;i < Data_Player.dataList.size();i++){
			Data_Player.dataList.get(i).update(event.renderTickTime);
		}
		
		for(int i = 0;i < Data_Zombie.dataList.size();i++){
			Data_Zombie.dataList.get(i).update(event.renderTickTime);
		}
		
		for(int i = 0;i < Data_Spider.dataList.size();i++){
			Data_Spider.dataList.get(i).update(event.renderTickTime);
		}
	}
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event){
		if(Minecraft.getMinecraft().theWorld == null){
			return;
		}
		
		//System.out.println((Minecraft.getMinecraft().getRenderManager().entityRenderMap.get(AbstractClientPlayer.class) instanceof RenderBendsPlayer));
		
		/*if(!(Minecraft.getMinecraft().getRenderManager().entityRenderMap.get(EntityPlayer.class) instanceof RenderBendsPlayer)){
			Render render = new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager());
			Minecraft.getMinecraft().getRenderManager().entityRenderMap.put(EntityPlayer.class, render);
		}
		
		if(!(Minecraft.getMinecraft().getRenderManager().entityRenderMap.get(AbstractClientPlayer.class) instanceof RenderBendsPlayer)){
			Render render = new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager());
			Minecraft.getMinecraft().getRenderManager().entityRenderMap.put(AbstractClientPlayer.class, render);
		}
		
		if(!(Minecraft.getMinecraft().getRenderManager().entityRenderMap.get(EntityPlayerSP.class) instanceof RenderBendsPlayer)){
			Render render = new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager());
			Minecraft.getMinecraft().getRenderManager().entityRenderMap.put(EntityPlayerSP.class, render);
		}*/
		
		for(int i = 0;i < Data_Player.dataList.size();i++){
			Data_Player data = Data_Player.dataList.get(i);
			Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(data.entityID);
			if(entity != null){
				if(!data.entityType.equalsIgnoreCase(entity.getName())){
					Data_Player.dataList.remove(data);
					Data_Player.add(new Data_Player(entity.getEntityId()));
					BendsLogger.log("Reset entity",BendsLogger.DEBUG);
				}else{
					
					data.motion_prev.set(data.motion);
					
					data.motion.x=(float) entity.posX-data.position.x;
					data.motion.y=(float) entity.posY-data.position.y;
					data.motion.z=(float) entity.posZ-data.position.z;
			    	
					data.position = new Vector3f((float)entity.posX,(float)entity.posY,(float)entity.posZ);
				}
			}else{
				Data_Player.dataList.remove(data);
				BendsLogger.log("No entity",BendsLogger.DEBUG);
			}
		}
		
		for(int i = 0;i < Data_Zombie.dataList.size();i++){
			Data_Zombie data = Data_Zombie.dataList.get(i);
			Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(data.entityID);
			if(entity != null){
				if(!data.entityType.equalsIgnoreCase(entity.getName())){
					Data_Zombie.dataList.remove(data);
					Data_Zombie.add(new Data_Zombie(entity.getEntityId()));
					BendsLogger.log("Reset entity",BendsLogger.DEBUG);
				}else{
					
					data.motion_prev.set(data.motion);
					
					data.motion.x=(float) entity.posX-data.position.x;
					data.motion.y=(float) entity.posY-data.position.y;
					data.motion.z=(float) entity.posZ-data.position.z;
			    	
					data.position = new Vector3f((float)entity.posX,(float)entity.posY,(float)entity.posZ);
				}
			}else{
				Data_Zombie.dataList.remove(data);
				BendsLogger.log("No entity",BendsLogger.DEBUG);
			}
		}
		
		for(int i = 0;i < Data_Spider.dataList.size();i++){
			Data_Spider data = Data_Spider.dataList.get(i);
			Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(data.entityID);
			if(entity != null){
				if(!data.entityType.equalsIgnoreCase(entity.getName())){
					Data_Spider.dataList.remove(data);
					Data_Spider.add(new Data_Spider(entity.getEntityId()));
					BendsLogger.log("Reset entity",BendsLogger.DEBUG);
				}else{
					
					data.motion_prev.set(data.motion);
					
					data.motion.x=(float) entity.posX-data.position.x;
					data.motion.y=(float) entity.posY-data.position.y;
					data.motion.z=(float) entity.posZ-data.position.z;
			    	
					data.position = new Vector3f((float)entity.posX,(float)entity.posY,(float)entity.posZ);
				}
			}else{
				Data_Spider.dataList.remove(data);
				BendsLogger.log("No entity",BendsLogger.DEBUG);
			}
		}
	}
}
