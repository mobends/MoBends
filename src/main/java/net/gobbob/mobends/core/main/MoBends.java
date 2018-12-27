package net.gobbob.mobends.core.main;

import net.gobbob.mobends.core.addon.AddonHelper;
import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;
import net.gobbob.mobends.core.configuration.ModConfiguration;
import net.gobbob.mobends.core.modcomp.RFPR;
import net.gobbob.mobends.core.network.msg.MessageClientConfigure;
import net.gobbob.mobends.standard.DefaultAddon;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = ModStatics.MODID, name = ModStatics.MODNAME, version = ModStatics.VERSION)
public class MoBends
{
	@SidedProxy(serverSide = "net.gobbob.mobends.core.main.CommonProxy", clientSide = "net.gobbob.mobends.core.client.ClientProxy")
	public static CommonProxy proxy;

	@Instance(value = ModStatics.MODID)
	public static MoBends instance;

	public SimpleNetworkWrapper networkWrapper;
	public ModConfiguration configuration;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		configuration = new ModConfiguration(event.getSuggestedConfigurationFile());
		Configuration config = configuration.getConfiguration();
		config.load();
		proxy.preInit(config);
		config.save();

		networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(ModStatics.MODID);
		networkWrapper.registerMessage(MessageClientConfigure.Handler.class, MessageClientConfigure.class, 0,
				Side.CLIENT);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		Configuration config = configuration.getConfiguration();

		AddonHelper.registerAddon(new DefaultAddon());
		
		config.load();
		proxy.init(config);
		config.save();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		Configuration config = configuration.getConfiguration();
		
		AnimatedEntityRegistry.applyConfiguration(config);
		RFPR.init();
	}
}
