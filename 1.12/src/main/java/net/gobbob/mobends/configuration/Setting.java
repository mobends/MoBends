package net.gobbob.mobends.settings;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.Configuration;

public abstract class SettingsNode<T> {
	protected String unlocalizedName;
	protected T data;
	protected T defaultValue;
	
	public SettingsNode(){
		this.unlocalizedName = "mobends.setting.null";
	}
	
	public SettingsNode(String unlocalizedName){
		this.unlocalizedName = "mobends.setting."+unlocalizedName;
	}
	
	public SettingsNode<T> setupDefault(T data){
		this.defaultValue = data;
		this.data = this.defaultValue;
		return this;
	}
	
	public T get(){
		return this.data;
	}
	
	public void set(T data) {
		this.data = data;
	}

	public String getUnlocalizedName() {
		return unlocalizedName;
	}

	public T getData() {
		return data;
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}

	public abstract void saveToConfig(Configuration config);
	public abstract void loadFromConfig(Configuration config);
}
