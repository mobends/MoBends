package goblinbob.mobends.standard.client.model.armor;

import net.minecraft.client.model.ModelBiped;

import java.util.HashMap;
import java.util.Map;

import goblinbob.mobends.standard.main.MoBends;

public class ArmorModelFactory
{
	protected static Map<ModelBiped, ArmorWrapper> wrapperArchive = new HashMap<>();

	/**
	 * NOTE: This might get called multiple times per frame, with differing "shouldBeMutated" values.
	 * 		 Hence why we don't demutate and delete the wrappers, just deapply them.
	 * @param suggested The model instance to mutate/demutate accordingly.
	 * @param shouldBeMutated Whether to return a vanilla model or the mutated model.
	 * @return A model that's altered appropriately.
	 */
	public static ModelBiped getArmorModel(ModelBiped suggested, boolean shouldBeMutated)
	{
		ArmorWrapper wrapper = wrapperArchive.get(suggested);

		if (shouldBeMutated)
		{
			if (wrapper == null)
			{
				wrapper = ArmorWrapper.createFor(suggested);
				wrapperArchive.put(suggested, wrapper);

				MoBends.LOG.info("Creating an armor wrapper for " + suggested);
			}

			return wrapper;
		}

		if (wrapper != null)
		{
			wrapper.deapply();
		}

		return suggested;
	}
	
	public static void refresh()
	{
		for (ArmorWrapper model : wrapperArchive.values())
		{
			model.demutate();
		}

		wrapperArchive.clear();
	}
}
