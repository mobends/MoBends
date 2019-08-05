package net.gobbob.mobends.core;

import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.client.event.EntityRenderHandler;
import net.gobbob.mobends.core.client.event.KeyboardHandler;
import net.gobbob.mobends.core.client.event.WorldJoinHandler;
import net.gobbob.mobends.core.configuration.CoreClientConfig;
import net.gobbob.mobends.core.modcomp.RFPR;
import net.gobbob.mobends.core.pack.PackManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CoreClient extends Core<CoreClientConfig>
{

	private CoreClientConfig configuration;

	@Override
	public CoreClientConfig getConfiguration()
	{
		return configuration;
	}

	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);

		configuration = new CoreClientConfig(event.getSuggestedConfigurationFile());
	}

	@Override
	public void init(FMLInitializationEvent event)
	{
		super.init(event);
		
		PackManager.instance.initialize(configuration);
		KeyboardHandler.initKeyBindings();

		MinecraftForge.EVENT_BUS.register(new EntityRenderHandler());
		MinecraftForge.EVENT_BUS.register(new DataUpdateHandler());
		MinecraftForge.EVENT_BUS.register(new KeyboardHandler());
		MinecraftForge.EVENT_BUS.register(new WorldJoinHandler());
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event)
	{
		super.postInit(event);
		
		AnimatedEntityRegistry.applyConfiguration(configuration);
		RFPR.init();
	}
	
}
