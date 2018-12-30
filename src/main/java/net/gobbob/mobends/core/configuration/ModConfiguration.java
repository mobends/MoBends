package net.gobbob.mobends.core.configuration;

import java.io.File;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;
import net.gobbob.mobends.core.pack.PackManager;
import net.minecraftforge.common.config.Configuration;

public class ModConfiguration
{
	protected File configFile;
	
	public ModConfiguration(File file)
	{
		configFile = file;
	}
	
	public void save()
	{
    	Configuration config = getConfiguration();
    	
    	config.load();
    	
    	for(AnimatedEntity<?> animatedEntity : AnimatedEntityRegistry.getRegistered()) {
    		for(AlterEntry<?> alterEntry : animatedEntity.getAlterEntries()) {
    			config.get("Animated", alterEntry.getKey(), true).setValue(alterEntry.isAnimated());
    		}
        }
    	
    	if(PackManager.getCurrentPack() != null)
    		config.get("General", "Current Pack", "none").setValue(PackManager.getCurrentPack().getFilename());
    	else
    		config.get("General", "Current Pack", "none").setValue("none");
    	
    	config.save();
    }
	
	public Configuration getConfiguration()
	{
		return new Configuration(configFile);
	}
}