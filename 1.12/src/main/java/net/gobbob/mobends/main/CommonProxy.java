package net.gobbob.mobends.main;

import net.gobbob.mobends.event.EventHandlerServer;
import net.gobbob.mobends.network.NetworkConfiguration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

public class CommonProxy {
	public void preInit(Configuration config) {
		preInitCommon(config);
		preInitServer(config);
	}
	
	public void preInitCommon(Configuration config) {
		
	}
	
	public void preInitServer(Configuration config) {
		MinecraftForge.EVENT_BUS.register(new EventHandlerServer());
		NetworkConfiguration.instance.allowModelScaling = config.getBoolean("AllowModelScaling", "Server", false, "Does the server allow scaling of the player model more than the normal size?");
	}
	
	public void init(Configuration config) {}
	public void postInit() {}
}
