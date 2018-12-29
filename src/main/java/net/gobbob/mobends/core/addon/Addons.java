package net.gobbob.mobends.core.addon;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.core.animatedentity.AddonAnimationRegistry;

public class Addons
{

	private static final Addons INSTANCE = new Addons();
	
	private Addons() {}
	
	private List<IAddon> addons = new ArrayList<IAddon>();
	
	static void registerAddon(String modid, IAddon addon)
	{
		if (INSTANCE.addons.contains(addon))
			return;
		
		INSTANCE.addons.add(addon);
		AddonAnimationRegistry registry = new AddonAnimationRegistry(modid);
		addon.registerAnimatedEntities(registry);
	}
	
	public static Iterable<IAddon> getRegistered()
	{
		return INSTANCE.addons;
	}
	
	public static void onRenderTick(float partialTicks)
	{
		INSTANCE.addons.forEach( addon -> addon.onRenderTick(partialTicks) );
	}

	public static void onClientTick()
	{
		INSTANCE.addons.forEach( addon -> addon.onClientTick() );
	}
	
}
