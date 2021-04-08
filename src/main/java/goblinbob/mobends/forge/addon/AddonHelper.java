package goblinbob.mobends.forge.addon;

/**
 * This class can be freely used by Addon developers.
 *
 * @author Iwo Plaza
 */
public class AddonHelper
{
	public static void registerAddon(String modId, IAddon addon)
	{
		Addons.registerAddon(modId, addon);
	}
}
