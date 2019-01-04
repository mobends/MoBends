package net.gobbob.mobends.core.addon;

/**
 * This class can be freely used by Addon developers.
 * 
 * @author Iwo Plaza
 *
 */
public class AddonHelper
{
	
	public static void registerAddon(String modid, IAddon addon)
	{
		Addons.registerAddon(modid, addon);
	}
	
}
