package net.gobbob.mobends.core.addon;

import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;

public interface IAddon
{
	void registerAnimatedEntities(AnimatedEntityRegistry registry);
	String getDisplayName();
}