package net.gobbob.mobends.core.client.model.entity.armor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.gobbob.mobends.data.BipedEntityData;
import net.minecraft.client.model.ModelBiped;

public class ArmorModelFactory
{
	protected static Map<ModelBiped, ModelBipedArmorCustom> customArchive = new HashMap<ModelBiped, ModelBipedArmorCustom>();

	public static ModelBiped getArmorModel(ModelBiped suggested)
	{
		ModelBiped custom = suggested;

		if (customArchive.containsKey(suggested))
		{
			custom = customArchive.get(suggested);
		}
		else
		{
			System.out.println("Creating a custom armor model from " + suggested);
			custom = ModelBipedArmorCustom.createFrom(suggested);
			customArchive.put(suggested, (ModelBipedArmorCustom) custom);
		}
		return custom;
	}

	/*
	 * This ensures that each armor's mutation state is
	 * in sync with it's AnimatedEntity counterpart.
	 */
	public static void updateMutation()
	{
		for (ModelBipedArmorCustom model : customArchive.values())
		{
			model.updateMutation();
		}
	}
	
	public static void refresh()
	{
		for (ModelBipedArmorCustom model : customArchive.values())
		{
			model.demutate();
		}
		customArchive.clear();
	}
}
