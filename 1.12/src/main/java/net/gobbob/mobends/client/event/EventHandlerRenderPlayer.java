package net.gobbob.mobends.client.event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.client.mutators.PlayerMutator;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.data.PlayerData;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventHandlerRenderPlayer
{
	
	public static float partialTicks;
	public static List<UUID> currentlyRenderedEntities = new ArrayList<UUID>();
	public static boolean renderingGuiScreen = false;
	
	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event)
	{
		partialTicks = event.renderTickTime;
	}
	
	@SubscribeEvent
	public void beforePlayerRender(RenderPlayerEvent.Pre event)
	{
		float pt = event.getPartialRenderTick();
		
		if (currentlyRenderedEntities.contains(event.getEntity().getUniqueID()))
			// The player is already being rendered.
			return;
		currentlyRenderedEntities.add(event.getEntity().getUniqueID());
		
		GlStateManager.pushMatrix();
		
		if(!(event.getEntity() instanceof EntityPlayer))
			return;
		if(AnimatedEntity.getByEntity(event.getEntity()) == null)
			return;
		
		if(AnimatedEntity.getByEntity(event.getEntity()).getAlterEntry(0).isAnimated())
		{
			AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
			PlayerMutator.apply(event.getRenderer(), player, pt);
			double yOffset = 0;

            if (player.isSneaking())
            {
            	yOffset = 0.155D * 2;
            }
            GlStateManager.translate(0, yOffset, 0);
            
            PlayerData data = (PlayerData) EntityDatabase.instance.getAndMake(PlayerData.class, player);
            GlStateManager.translate(data.renderOffset.getX() * 0.0625F, data.renderOffset.getY() * 0.0625F, data.renderOffset.getZ() * 0.0625F);
            GlStateManager.rotate(data.renderRotation.getZ(), 0F, 0F, 1F);
            GlStateManager.rotate(data.renderRotation.getY(), 0F, 1F, 0F);
            GlStateManager.rotate(data.renderRotation.getX(), 1F, 0F, 0F);
		}
	}
	
	@SubscribeEvent
	public void afterPlayerRender(RenderPlayerEvent.Post event)
	{
		if (!currentlyRenderedEntities.contains(event.getEntity().getUniqueID()))
			// The player is not being rendered.
			return;
		
		GlStateManager.popMatrix();
		currentlyRenderedEntities.remove(event.getEntity().getUniqueID());
	}
	
	@SubscribeEvent
	public void beforeGuiScreenRender(GuiScreenEvent.DrawScreenEvent.Pre event)
	{
		renderingGuiScreen = true;
	}
	
	@SubscribeEvent
	public void afterGuiScreenRender(GuiScreenEvent.DrawScreenEvent.Post event)
	{
		renderingGuiScreen = false;
	}
}
