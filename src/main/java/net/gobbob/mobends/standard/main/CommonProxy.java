package net.gobbob.mobends.standard.main;

import net.gobbob.mobends.core.Core;
import net.gobbob.mobends.core.CoreServer;
import net.gobbob.mobends.core.event.EventHandlerServer;
import net.gobbob.mobends.core.network.NetworkConfiguration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

public class CommonProxy
{
	public void preInit() {}
	public void init() {}
	public void postInit() {}

	public void createCore()
	{
		Core.createAsServer();
	}
}
