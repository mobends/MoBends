package net.gobbob.mobends.settings;

import net.minecraftforge.common.config.Configuration;

public class SettingManager {
	public static final SettingsBoolean SWORD_TRAIL = (SettingsBoolean) new SettingsBoolean("sword_trail").setupDefault(true);
	public static final SettingsBoolean ARROW_TRAILS = (SettingsBoolean) new SettingsBoolean("arrow_trails").setupDefault(true);
	public static final SettingsBoolean SPIN_ATTACK = (SettingsBoolean) new SettingsBoolean("spin_attack").setupDefault(true);
	
	public static SettingsNode[] settings = new SettingsNode[]{
		SWORD_TRAIL, ARROW_TRAILS, SPIN_ATTACK
	};
	
	public static void saveConfiguration(Configuration config) {
		for(SettingsNode node : settings) {
			node.saveToConfig(config);
		}
	}

	public static void loadConfiguration(Configuration config) {
		for(SettingsNode node : settings) {
			node.loadFromConfig(config);
		}
	}
}