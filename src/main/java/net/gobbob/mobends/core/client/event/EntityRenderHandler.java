package net.gobbob.mobends.core.client.event;

import java.util.HashSet;
import java.util.UUID;

import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.standard.client.mutators.PlayerMutator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityRenderHandler
{
	public static boolean renderingGuiScreen = false;
	private HashSet<UUID> currentlyRenderedEntities = new HashSet<>();
	
	@SubscribeEvent
	public void beforeLivingRender(RenderLivingEvent.Pre<? extends EntityLivingBase> event)
	{
		AnimatedEntity<EntityLivingBase> animatedEntity = AnimatedEntity.getForEntity(event.getEntity());
		if (animatedEntity == null)
			return;
		
		if (currentlyRenderedEntities.contains(event.getEntity().getUniqueID()))
			// The entity is already being rendered.
			return;
		currentlyRenderedEntities.add(event.getEntity().getUniqueID());

		EntityLivingBase living = (EntityLivingBase) event.getEntity();
		float pt = event.getPartialRenderTick();

		GlStateManager.pushMatrix();
		
		if (animatedEntity.isAnimated())
		{
			animatedEntity.applyMutation(event.getRenderer(), living, pt);
			animatedEntity.beforeRender(living, pt);
		}
		else
		{
			animatedEntity.deapplyMutation(event.getRenderer(), living);
		}
	}

	@SubscribeEvent
	public void afterLivingRender(RenderLivingEvent.Post<? extends EntityLivingBase> event)
	{
		AnimatedEntity<EntityLivingBase> animatedEntity = AnimatedEntity.getForEntity(event.getEntity());
		if (animatedEntity == null)
			return;

		if (!currentlyRenderedEntities.contains(event.getEntity().getUniqueID()))
			// The entity is not being rendered.
			return;
		currentlyRenderedEntities.remove(event.getEntity().getUniqueID());

		animatedEntity.afterRender(event.getEntity(), event.getPartialRenderTick());
		
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
	
	@SubscribeEvent
	public void beforeHandRender(RenderHandEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		Entity viewEntity = mc.getRenderViewEntity();
		if (!(viewEntity instanceof AbstractClientPlayer))
			return;
		AbstractClientPlayer player = (AbstractClientPlayer) viewEntity;
		AnimatedEntity<AbstractClientPlayer> animatedEntity = AnimatedEntity.getForEntity(player);
		
		if (animatedEntity != null && animatedEntity.isAnimated())
		{
			Render<Entity> render = mc.getRenderManager().getEntityRenderObject(viewEntity);
			PlayerMutator mutator = PlayerMutator.getMutatorForRenderer(render);
			if (mutator != null)
				mutator.poseForFirstPersonView();
		}
	}
}
