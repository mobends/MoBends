package goblinbob.mobends.core.addon;

import goblinbob.mobends.core.bender.AddonAnimationRegistry;

/**
 * Classes that implement this interface can be
 * registered as an addon.
 * 
 * @author Iwo Plaza
 */
public interface IAddon
{
	
	void registerAnimatedEntities(AddonAnimationRegistry registry);
	
	String getDisplayName();
	
	/**
	 * Called on each frame during rendering.
	 * @param partialTicks A value between 0-1 describing how
	 * 					   close we are to the next client tick.
	 */
	default void onRenderTick(float partialTicks) {}
	
	/**
	 * Called on the regular Minecraft tick.
	 */
	default void onClientTick() {}
	
	/**
	 * Called when the core is being refreshed. This is where all
	 * cached resources should be freed.
	 */
	default void onRefresh() {}
	
}