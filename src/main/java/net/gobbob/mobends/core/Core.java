package net.gobbob.mobends.core;

import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.client.event.EntityRenderHandler;
import net.gobbob.mobends.core.client.event.KeyboardHandler;
import net.gobbob.mobends.core.configuration.ModConfiguration;
import net.gobbob.mobends.core.network.msg.MessageClientConfigure;
import net.gobbob.mobends.core.pack.PackManager;
import net.gobbob.mobends.core.pack.variable.BendsVariable;
import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class Core
{
	public static Core INSTANCE;
	
	public SimpleNetworkWrapper networkWrapper;
	public ModConfiguration configuration;
	
	public void preInit(FMLPreInitializationEvent event)
	{
		configuration = new ModConfiguration(event.getSuggestedConfigurationFile());
		
		networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(ModStatics.MODID);
		networkWrapper.registerMessage(MessageClientConfigure.Handler.class, MessageClientConfigure.class, 0,
				Side.CLIENT);
	}
	
	public void init(FMLInitializationEvent event)
	{
		
	}
	
	public void postInit(FMLPostInitializationEvent event)
	{
		
	}
	
	public static void createAsClient()
	{
		if (INSTANCE == null)
			INSTANCE = new CoreClient();
	}
	
	public static void createAsServer()
	{
		if (INSTANCE == null)
			INSTANCE = new CoreServer();
	}
	
	public static SimpleNetworkWrapper getNetworkWrapper()
	{
		return INSTANCE.networkWrapper;
	}

	public static void saveConfiguration()
	{
		INSTANCE.configuration.save();
	}
}
