package net.gobbob.mobends.settings;

public class SettingsBoolean extends SettingsNode{

	public boolean data;
	public boolean defaultData;
	
	public SettingsBoolean(){
		super();
	}
	
	public SettingsBoolean(String argID,String argDisplayName){
		super(argID,argDisplayName);
	}
	
	public void setData(boolean argData){
		this.data = argData;
	}
	
	public SettingsBoolean setupDefault(boolean argData){
		this.data = argData;
		this.defaultData = argData;
		return this;
	}
}
