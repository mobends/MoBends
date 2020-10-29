package goblinbob.mobends.standard.client.model.armor;

import net.minecraft.client.model.ModelBiped;

import java.util.HashMap;
import java.util.Map;

public class ArmorModelFactory
{
	
	protected static Map<ModelBiped, MutatedArmorModel> customArchive = new HashMap<>();

	public static ModelBiped getArmorModel(ModelBiped suggested)
	{
		if (customArchive.containsKey(suggested))
		{
			return customArchive.get(suggested);
		}
		else
		{
			final MutatedArmorModel custom = MutatedArmorModel.createFrom(suggested);

			System.out.println("Creating a custom armor model from " + suggested);

			customArchive.put(suggested, custom);

			return custom;
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
