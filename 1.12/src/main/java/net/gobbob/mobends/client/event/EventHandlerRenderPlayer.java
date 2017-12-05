package net.gobbob.mobends.client.event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.client.mutators.MutatorPlayer;
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
		GlStateManager.pushMatrix();
		
		if(!(event.getEntity() instanceof EntityPlayer))
			return;
		if(AnimatedEntity.getByEntity(event.getEntity()) == null)
			return;
		
		if(AnimatedEntity.getByEntity(event.getEntity()).getAlterEntry(0).isAnimated())
		{
			AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
			MutatorPlayer.apply(event.getRenderer(), player, pt);
			double yOffset = 0;

            if (player.isSneaking())
            {
            	yOffset = 0.145D;
            }
            GlStateManager.translate(0, yOffset, 0);
		}
	}
	
	@SubscribeEvent
	public void afterPlayerRender(RenderPlayerEvent.Post event) {
		GlStateManager.popMatrix();
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
