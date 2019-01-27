package net.gobbob.mobends.standard.client.model.armor;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.model.ModelBiped;

public class ArmorModelFactory
{
	
	protected static Map<ModelBiped, MutatedArmorModel> customArchive = new HashMap<ModelBiped, MutatedArmorModel>();

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
			custom = MutatedArmorModel.createFrom(suggested);
			customArchive.put(suggested, (MutatedArmorModel) custom);
		}
		return custom;
	}

	/**
	 * This ensures that each armor's mutation state is
	 * in sync with it's AnimatedEntity counterpart.
	 */
	public static void updateMutation()
	{
		for (MutatedArmorModel model : customArchive.values())
		{
			model.updateMutation();
		}
	}
	
	public static void refresh()
	{
		for (MutatedArmorModel model : customArchive.values())
		{
			model.demutate();
		}
		customArchive.clear();
	}

}
