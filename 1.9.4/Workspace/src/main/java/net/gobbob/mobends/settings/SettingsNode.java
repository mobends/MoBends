package net.gobbob.mobends.settings;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;

public abstract class SettingsNode {
	public static SettingsNode[] settings = new SettingsNode[]{
		new SettingsBoolean("swordTrail","Sword Trail").setupDefault(true),
		new SettingsBoolean("thirdSwordAttack","Spin Attack").setupDefault(true),
	};
	
	public String id;
	public String displayName;
	
	public SettingsNode(){
		this.id = "NULL";
		this.displayName = "NULL";
	}
	
	public SettingsNode(String argID,String argDisplayName){
		this.id = argID;
		this.displayName = argDisplayName;
	}
	
	public static SettingsNode getSetting(String argID){
		for(int i = 0;i < settings.length;i++){
			if(settings[i].id.equalsIgnoreCase(argID)){
				return settings[i];
			}
		}
		return null;
	}
	
	public boolean getBoolean(){
		return ((SettingsBoolean)this).data;
	}
}
