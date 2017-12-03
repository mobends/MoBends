package net.gobbob.mobends.client.event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.client.renderer.entity.RenderBendsPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class PlayerRenderingHandler {
	public static List<UUID> currentlyRenderedEntities = new ArrayList<UUID>();
	
	@SubscribeEvent
	public void onPlayerRender(RenderPlayerEvent.Pre event){
		if(!(event.getEntity() instanceof EntityPlayer)){
			return;
		}
		
		if(AnimatedEntity.getByEntity(event.getEntity()) == null){
			return;
		}
		
		if(AnimatedEntity.getByEntity(event.getEntity()).getAlterEntry(0).isAnimated()){
			AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
			
			if(!currentlyRenderedEntities.contains(event.getEntity().getUniqueID())){
				currentlyRenderedEntities.add(event.getEntity().getUniqueID());
				event.setCanceled(true);
				
				RenderBendsPlayer renderer = AnimatedEntity.getPlayerRenderer(player);
				ModelBendsPlayer model = (ModelBendsPlayer) renderer.getMainModel();
				
				if(Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && !DataUpdateHandler.renderingGuiScreen && player instanceof EntityPlayerSP){
					model.bipedHead.isHidden = true;
					model.bipedHeadwear.isHidden = true;
				}else{
					model.bipedHead.isHidden = false;
					model.bipedHeadwear.isHidden = false;
				}
				
				float entityYaw = event.getEntity().prevRotationYaw + (event.getEntity().rotationYaw - event.getEntity().prevRotationYaw) * event.getPartialRenderTick();
				AnimatedEntity.getPlayerRenderer(player).doRender(player, event.getX(), event.getY(), event.getZ(), entityYaw, event.getPartialRenderTick());
				currentlyRenderedEntities.remove(event.getEntity().getUniqueID());
			}
		}
	}
}
