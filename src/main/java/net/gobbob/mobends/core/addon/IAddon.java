package net.gobbob.mobends.addon;

import net.gobbob.mobends.animatedentity.AnimatedEntityRegistry;

public interface IAddon
{
	
	void registerAnimatedEntities(AnimatedEntityRegistry registry);
	String getDisplayName();
	
}