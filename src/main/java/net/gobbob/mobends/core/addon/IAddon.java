package net.gobbob.mobends.core.addon;

import net.gobbob.mobends.core.animatedentity.AddonAnimationRegistry;

public interface IAddon
{
	
	void registerAnimatedEntities(AddonAnimationRegistry registry);
	String getDisplayName();
	
}