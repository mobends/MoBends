package net.gobbob.mobends.standard.client;

import net.gobbob.mobends.core.Core;
import net.gobbob.mobends.standard.client.event.RenderingEventHandler;
import net.gobbob.mobends.standard.client.renderer.entity.RenderBendsSpectralArrow;
import net.gobbob.mobends.standard.client.renderer.entity.RenderBendsTippedArrow;
import net.gobbob.mobends.standard.main.CommonProxy;
import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
	
	public void preInit(Configuration config)
	{
		RenderingRegistry.registerEntityRenderingHandler(EntitySpectralArrow.class, RenderBendsSpectralArrow::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTippedArrow.class, RenderBendsTippedArrow::new);
	}

	public void init()
	{
		MinecraftForge.EVENT_BUS.register(new RenderingEventHandler());
	}
	
	public void postInit() {}
	
	@Override
	public void createCore()
	{
		Core.createAsClient();
	}
}
