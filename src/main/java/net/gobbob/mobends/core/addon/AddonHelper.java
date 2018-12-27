package net.gobbob.mobends.core.addon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;

public class AddonHelper
{
	private static final AddonHelper INSTANCE = new AddonHelper();
	
	private List<IAddon> addons = new ArrayList<IAddon>();
	
	public static void registerAddon(IAddon addon)
	{
		if (INSTANCE.addons.contains(addon))
			return;
		
		INSTANCE.addons.add(addon);
		addon.registerAnimatedEntities(AnimatedEntityRegistry.INSTANCE);
	}
	
	public static Iterable<IAddon> getRegistered()
	{
		return INSTANCE.addons;
	}
}
