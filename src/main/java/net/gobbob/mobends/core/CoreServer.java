package net.gobbob.mobends.core;

import net.gobbob.mobends.core.configuration.CoreServerConfig;
import net.gobbob.mobends.core.event.EventHandlerServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CoreServer extends Core<CoreServerConfig>
{

	CoreServerConfig configuration;

	@Override
	public CoreServerConfig getConfiguration()
	{
		return configuration;
	}

	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);

		configuration = new CoreServerConfig(event.getSuggestedConfigurationFile());

		MinecraftForge.EVENT_BUS.register(new EventHandlerServer());
	}
	
}
