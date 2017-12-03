package net.gobbob.mobends.configuration;

import net.minecraftforge.common.config.Configuration;

public class SettingBoolean extends Setting<Boolean> {
	public SettingBoolean(){
		super();
	}
	
	public SettingBoolean(String unlocalizedName){
		super(unlocalizedName);
	}

	@Override
	public void saveToConfig(Configuration config) {
		config.get("General", getUnlocalizedName(), getDefaultValue()).set(getData());
	}

	@Override
	public void loadFromConfig(Configuration config) {
		this.value = config.get("General", getUnlocalizedName(), getDefaultValue()).getBoolean();
	}

	public boolean isEnabled() {
		return this.value;
	}

	public void toggle() {
		this.value = !this.value;
	}
}
