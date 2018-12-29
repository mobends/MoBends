package net.gobbob.mobends.core.addon;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.core.animatedentity.AddonAnimationRegistry;
import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;

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
