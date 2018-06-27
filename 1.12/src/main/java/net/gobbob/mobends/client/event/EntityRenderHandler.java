package net.gobbob.mobends.client.event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.client.mutators.PlayerMutator;
import net.gobbob.mobends.client.mutators.ZombieMutator;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.main.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.client.event.GuiScreenEvent;
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
		AnimatedEntity animatedEntity = AnimatedEntity.getByEntity(event.getEntity());
		if (animatedEntity == null)
			return;

		if (currentlyRenderedEntities.contains(event.getEntity().getUniqueID()))
			// The entity is already being rendered.
			return;
		currentlyRenderedEntities.add(event.getEntity().getUniqueID());

		EntityLivingBase living = (EntityLivingBase) event.getEntity();
		float pt = event.getPartialRenderTick();

		GlStateManager.pushMatrix();

		if (animatedEntity.getAlterEntry(0).isAnimated())
		{
			animatedEntity.applyMutation(event.getRenderer(), living, pt);

			EntityData data = EntityDatabase.instance.get(living);
			float scale = 0.0625F;

			Entity viewEntity = Minecraft.getMinecraft().getRenderViewEntity();
			double viewX = viewEntity.prevPosX + (viewEntity.posX - viewEntity.prevPosX) * pt;
			double viewY = viewEntity.prevPosY + (viewEntity.posY - viewEntity.prevPosY) * pt;
			double viewZ = viewEntity.prevPosZ + (viewEntity.posZ - viewEntity.prevPosZ) * pt;
			double entityX = living.prevPosX + (living.posX - living.prevPosX) * pt;
			double entityY = living.prevPosY + (living.posY - living.prevPosY) * pt;
			double entityZ = living.prevPosZ + (living.posZ - living.prevPosZ) * pt;
			GlStateManager.translate(entityX - viewX, entityY - viewY, entityZ - viewZ);

			if (data instanceof BipedEntityData)
			{
				BipedEntityData bipedData = (BipedEntityData) data;
				if (ModConfig.showSwordTrail)
				{
					GlStateManager.pushMatrix();
					GlStateManager.rotate(-living.rotationYaw + 180.0F, 0F, 1F, 0F);
					GlStateManager.scale(scale, scale, scale);
					bipedData.swordTrail.render();
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
					GlStateManager.popMatrix();
				}

				if (living.isSneaking())
				{
					GlStateManager.translate(0, 0.155D * 2, 0);
				}

				GlStateManager.translate(bipedData.renderOffset.getX() * scale, bipedData.renderOffset.getY() * scale,
						bipedData.renderOffset.getZ() * scale);
				GlStateManager.rotate(bipedData.renderRotation.getZ(), 0F, 0F, 1F);
				GlStateManager.rotate(bipedData.renderRotation.getY(), 0F, 1F, 0F);
				GlStateManager.rotate(bipedData.renderRotation.getX(), 1F, 0F, 0F);
			}
			GlStateManager.translate(viewX - entityX, viewY - entityY, viewZ - entityZ);
		}
		else
		{
			animatedEntity.deapplyMutation(event.getRenderer(), living);
		}
	}

	@SubscribeEvent
	public void afterLivingRender(RenderLivingEvent.Post<? extends EntityLivingBase> event)
	{
		if (AnimatedEntity.getByEntity(event.getEntity()) == null)
			return;

		if (!currentlyRenderedEntities.contains(event.getEntity().getUniqueID()))
			// The entity is not being rendered.
			return;
		currentlyRenderedEntities.remove(event.getEntity().getUniqueID());

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
}
