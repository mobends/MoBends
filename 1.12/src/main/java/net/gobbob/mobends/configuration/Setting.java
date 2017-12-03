package net.gobbob.mobends.configuration;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.Configuration;

public abstract class Setting<T> {
	protected String unlocalizedName;
	protected T value;
	protected T defaultValue;
	
	public Setting(){
		this.unlocalizedName = "mobends.setting.null";
	}
	
	public Setting(String unlocalizedName){
		this.unlocalizedName = "mobends.setting."+unlocalizedName;
	}
	
	public Setting<T> setupDefault(T data){
		this.defaultValue = data;
		this.value = this.defaultValue;
		return this;
	}
	
	public T get(){
		return this.value;
	}
	
	public void set(T data) {
		this.value = data;
	}

	public String getUnlocalizedName() {
		return unlocalizedName;
	}

	public T getData() {
		return value;
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}

	public abstract void saveToConfig(Configuration config);
	public abstract void loadFromConfig(Configuration config);
}
