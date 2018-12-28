package net.gobbob.mobends.core.client.event;

import java.util.HashSet;
import java.util.UUID;

import net.gobbob.mobends.core.EntityData;
import net.gobbob.mobends.core.EntityDatabase;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.standard.client.mutators.PlayerMutator;
import net.gobbob.mobends.standard.data.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class EntityRenderHandler
{
	public static boolean renderingGuiScreen = false;
	private HashSet<UUID> currentlyRenderedEntities = new HashSet<>();
	
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
		
		EntityEntry entry = EntityRegistry.getEntry(AbstractClientPlayer.class);
		if (entry != null)
			System.out.println(entry.getName());
		
		ResourceLocation location = EntityList.getKey(EntitySpider.class);
		if (location != null)
		{
			String s1 = EntityList.getTranslationName(location);
			System.out.println(I18n.translateToLocal("entity." + s1 + ".name"));
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
			Render<Entity> render = mc.getRenderManager().getEntityRenderObject(viewEntity);
			if (render instanceof RenderLivingBase)
			{
				PlayerMutator mutator = (PlayerMutator) AnimatedEntity.getMutatorForRenderer(AbstractClientPlayer.class, (RenderLivingBase) render);
				if (mutator != null)
					mutator.poseForFirstPersonView();
			}
		}
	}
}
