package net.gobbob.mobends.configuration;

import net.minecraftforge.common.config.Configuration;

public class SettingsManager {
	public static final SettingBoolean SWORD_TRAIL = (SettingBoolean) new SettingBoolean("sword_trail").setupDefault(true);
	public static final SettingBoolean ARROW_TRAILS = (SettingBoolean) new SettingBoolean("arrow_trails").setupDefault(true);
	public static final SettingBoolean SPIN_ATTACK = (SettingBoolean) new SettingBoolean("spin_attack").setupDefault(true);
	
	public static Setting[] settings = new Setting[]{
		SWORD_TRAIL, ARROW_TRAILS, SPIN_ATTACK
	};
	
	public static void saveConfiguration(Configuration config) {
		for(Setting node : settings) {
			node.saveToConfig(config);
		}
	}

	public static void loadConfiguration(Configuration config) {
		for(Setting node : settings) {
			node.loadFromConfig(config);
		}
	}
}