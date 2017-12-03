package net.gobbob.mobends.main;

import java.io.File;
import java.util.List;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.animatedentity.alterentry.AlterEntry;
import net.gobbob.mobends.configuration.SettingsManager;
import net.gobbob.mobends.configuration.SettingBoolean;
import net.gobbob.mobends.configuration.ModConfiguration;
import net.gobbob.mobends.configuration.Setting;
import net.gobbob.mobends.modcomp.RFPR;
import net.gobbob.mobends.network.msg.MessageClientConfigure;
import net.gobbob.mobends.pack.PackManager;
import net.minecraft.init.Blocks;
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
public class ModBase
{
    @SidedProxy(serverSide="net.gobbob.mobends.main.CommonProxy", clientSide="net.gobbob.mobends.client.ClientProxy")
    public static CommonProxy proxy;
    
    @Instance(value=ModStatics.MODID)
    public static ModBase instance;
    
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
        networkWrapper.registerMessage(MessageClientConfigure.Handler.class, MessageClientConfigure.class, 0, Side.CLIENT);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	Configuration config = configuration.getConfiguration();
    	
    	config.load();
    	proxy.init(config);
    	config.save();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	RFPR.init();
    }
}
