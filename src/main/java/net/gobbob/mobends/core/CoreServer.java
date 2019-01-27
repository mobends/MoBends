package net.gobbob.mobends.core;

import net.gobbob.mobends.core.event.EventHandlerServer;
import net.gobbob.mobends.core.network.NetworkConfiguration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CoreServer extends Core
{
	
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);
		
		Configuration config = configuration.getConfiguration();
		
		MinecraftForge.EVENT_BUS.register(new EventHandlerServer());
		NetworkConfiguration.instance.allowModelScaling = config.getBoolean("AllowModelScaling", "Server", false,
				"Does the server allow scaling of the player model more than the normal size?");
	}
	
}
