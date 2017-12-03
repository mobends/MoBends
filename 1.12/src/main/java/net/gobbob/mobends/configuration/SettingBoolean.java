package net.gobbob.mobends.settings;

import net.minecraftforge.common.config.Configuration;

public class SettingsBoolean extends SettingsNode<Boolean> {
	public SettingsBoolean(){
		super();
	}
	
	public SettingsBoolean(String unlocalizedName){
		super(unlocalizedName);
	}

	@Override
	public void saveToConfig(Configuration config) {
		config.get("General", getUnlocalizedName(), getDefaultValue()).set(getData());
	}

	@Override
	public void loadFromConfig(Configuration config) {
		this.data = config.get("General", getUnlocalizedName(), getDefaultValue()).getBoolean();
	}

	public boolean isEnabled() {
		return this.data;
	}

	public void toggle() {
		this.data = !this.data;
	}
}
