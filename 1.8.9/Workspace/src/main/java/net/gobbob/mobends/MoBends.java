package net.gobbob.mobends;

import java.io.File;

import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.settings.SettingsBoolean;
import net.gobbob.mobends.settings.SettingsNode;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = MoBends.MODID, version = MoBends.VERSION)
public class MoBends
{
    public static final String MODID = "mobends";
    public static final String MODNAME = "Mo' Bends";
    public static final String VERSION = "0.21.4";
    
    @SidedProxy(serverSide="net.gobbob.mobends.CommonProxy", clientSide="net.gobbob.mobends.client.ClientProxy")
    public static CommonProxy proxy;
    
    @Instance(value=MODID)
    public static MoBends instance;
    
    public static File configFile;
    
    public static int refreshModel = 0;
    
    @EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {
    	configFile = event.getSuggestedConfigurationFile();
    	Configuration config = new Configuration(event.getSuggestedConfigurationFile());

        config.load();
        
        proxy.preinit(config);
        
        config.save();
    }
    
    public static void saveConfig(){
    	Configuration config = new Configuration(configFile);
    	
    	config.load();
    	
    	for(int i = 0;i < AnimatedEntity.animatedEntities.size();i++){
        	config.get("Animate", AnimatedEntity.animatedEntities.get(i).id, false).setValue(AnimatedEntity.animatedEntities.get(i).animate);
        }
    	
    	config.get("General", "Sword Trail", true).setValue(((SettingsBoolean)SettingsNode.getSetting("swordTrail")).data);
    	config.get("General", "Current Pack", true).setValue(BendsPack.currentPack);
    	
    	config.save();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	Configuration config = new Configuration(configFile);
    	
    	config.load();
    	
    	proxy.init(config);
    	
    	config.save();
    }
}
