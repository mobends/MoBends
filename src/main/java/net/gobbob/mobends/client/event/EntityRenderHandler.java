package net.gobbob.mobends.client.event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector4f;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.client.mutators.PlayerMutator;
import net.gobbob.mobends.client.mutators.ZombieMutator;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.main.ModConfig;
import net.gobbob.mobends.util.Color;
import net.gobbob.mobends.util.Draw;
import net.gobbob.mobends.util.GLHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EntityRenderHandler
{
	public static float partialTicks;
	public static boolean renderingGuiScreen = false;
	public static List<UUID> currentlyRenderedEntities = new ArrayList<UUID>();
	
	@SubscribeEvent
	public void beforeLivingRender(RenderLivingEvent.Pre<? extends EntityLivingBase> event)
	{
		AnimatedEntity animatedEntity = AnimatedEntity.getForEntity(event.getEntity());
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
		AnimatedEntity animatedEntity = AnimatedEntity.getForEntity(event.getEntity());
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
	public void onRenderTick(TickEvent.RenderTickEvent event)
	{
		partialTicks = event.renderTickTime;
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
		AnimatedEntity animatedEntity = AnimatedEntity.getForEntity(viewEntity);
		EntityData entityData = EntityDatabase.instance.get(viewEntity);
		
		if (animatedEntity != null && animatedEntity.isAnimated() && entityData instanceof PlayerData)
		{
			Render<AbstractClientPlayer> render = mc.getRenderManager().<AbstractClientPlayer>getEntityRenderObject(viewEntity);
			PlayerMutator mutator = PlayerMutator.getMutatorForRenderer(render);
			if (mutator != null)
				mutator.poseForFirstPersonView();
		}
	}
}
