package net.gobbob.mobends.client.model.entity.armor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.model.ModelBiped;

public class ArmorModelFactory
{
	protected static Map<Integer, ModelBiped> customArchive = new HashMap<Integer, ModelBiped>();
	
	public static ModelBiped getArmorModel(ModelBiped suggested) {
		ModelBiped custom = suggested;
		
		if(customArchive.containsKey(suggested.hashCode())) {
			custom = customArchive.get(suggested.hashCode());
		}else {
			System.out.println("Creating a custom armor model." + suggested);
			custom = ModelBipedArmorCustom.createFrom(suggested);
			customArchive.put(suggested.hashCode(), custom);
		}
		return custom;
	}
	
	public static void refresh() {
		customArchive.clear();
	}
}
