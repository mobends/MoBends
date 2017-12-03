package net.gobbob.mobends.main;

import java.io.File;
import java.util.List;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.animatedentity.alterentry.AlterEntry;
import net.gobbob.mobends.configuration.SettingsManager;
import net.gobbob.mobends.configuration.SettingBoolean;
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
    @SidedProxy(serverSide="net.gobbob.mobends.CommonProxy", clientSide="net.gobbob.mobends.client.ClientProxy")
    public static CommonProxy proxy;
    
    @Instance(value=ModStatics.MODID)
    public static ModBase instance;
    
    public static File configFile;
	public static int refreshModel = 0;
    
	public static SimpleNetworkWrapper networkWrapper;
	
    @EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {
    	configFile = event.getSuggestedConfigurationFile();
    	Configuration config = new Configuration(event.getSuggestedConfigurationFile());

        config.load();
        proxy.preInit(config);
        config.save();
        
        networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(ModStatics.MODID);
        networkWrapper.registerMessage(MessageClientConfigure.Handler.class, MessageClientConfigure.class, 0, Side.CLIENT);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	Configuration config = new Configuration(configFile);
    	
    	config.load();
    	proxy.init(config);
    	config.save();
    }
    
    @EventHandler
    public void postinit(FMLPostInitializationEvent event)
    {
    	RFPR.init();
    }
    
    public static void saveConfig(){
    	Configuration config = new Configuration(configFile);
    	
    	config.load();
    	
    	for(AnimatedEntity animatedEntity : AnimatedEntity.animatedEntities.values()){
    		List<AlterEntry> alterEntries = animatedEntity.getAlredEntries();
    		for(int a = 0; a < alterEntries.size(); a++){
    			config.get("Animated", alterEntries.get(a).getName(), true).setValue(alterEntries.get(a).isAnimated());
    		}
        }
    	
    	SettingsManager.saveConfiguration(config);
    	
    	if(PackManager.getCurrentPack() != null)
    		config.get("General", "Current Pack", "none").setValue(PackManager.getCurrentPack().getFilename());
    	else
    		config.get("General", "Current Pack", "none").setValue("none");
    	
    	config.save();
    }
}
