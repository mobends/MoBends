package net.gobbob.mobends.configuration;

import java.io.File;
import java.util.List;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.animatedentity.alterentry.AlterEntry;
import net.gobbob.mobends.main.ModBase;
import net.gobbob.mobends.pack.PackManager;
import net.minecraftforge.common.config.Configuration;

public class ModConfiguration {
	protected File configFile;
	
	public ModConfiguration(File file) {
		configFile = file;
    	Configuration config = getConfiguration();

        config.load();
        ModBase.instance.proxy.preInit(config);
        config.save();
	}
	
	public void save() {
    	Configuration config = getConfiguration();
    	
    	config.load();
    	
    	for(AnimatedEntity animatedEntity : AnimatedEntity.animatedEntities.values()) {
    		List<AlterEntry> alterEntries = animatedEntity.getAlredEntries();
    		for(int a = 0; a < alterEntries.size(); a++) {
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
	
	public Configuration getConfiguration() {
		return new Configuration(configFile);
	}
}