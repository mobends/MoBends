package net.gobbob.mobends.core.addon;

import net.gobbob.mobends.core.CoreClient;
import net.gobbob.mobends.core.animatedentity.AddonAnimationRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * The class responsible for managing the registered addons.
 * 
 * -- FOR ADDON DEVELOPERS: --
 * Don't use this class directly. Use the AddonHelper class instead.
 * 
 * @author Iwo Plaza
 *
 */
public class Addons
{

	private static final Addons INSTANCE = new Addons();
	
	private Addons() {}
	
	private List<IAddon> addons = new ArrayList<>();
	
	static void registerAddon(String modid, IAddon addon)
	{
		if (INSTANCE.addons.contains(addon))
			return;
		
		INSTANCE.addons.add(addon);

		if (CoreClient.getInstance() != null)
		{
			addon.registerAnimatedEntities(new AddonAnimationRegistry(modid));
		}
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
	
	public static void onRefresh()
	{
		INSTANCE.addons.forEach( addon -> addon.onRefresh() );
	}
	
}
