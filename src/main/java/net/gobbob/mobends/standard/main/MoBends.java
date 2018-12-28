package net.gobbob.mobends.standard.main;

import net.gobbob.mobends.core.Core;
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
	@SidedProxy(serverSide = "net.gobbob.mobends.standard.main.CommonProxy", clientSide = "net.gobbob.mobends.standard.client.ClientProxy")
	public static CommonProxy proxy;

	@Instance(value = ModStatics.MODID)
	public static MoBends instance;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.createCore();
		Core.INSTANCE.preInit(event);
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		Core.INSTANCE.init(event);
		proxy.init();
		
		// Registering the standard set of animations.
		AddonHelper.registerAddon(ModStatics.MODID, new DefaultAddon());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		Core.INSTANCE.postInit(event);
		proxy.postInit();
	}
}
