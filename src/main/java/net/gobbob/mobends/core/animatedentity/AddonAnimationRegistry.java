package net.gobbob.mobends.core.animatedentity;

import net.gobbob.mobends.core.util.BendsLogger;

public class AddonAnimationRegistry
{
	
	private String modid;
	
	public AddonAnimationRegistry(String modid)
	{
		this.modid = modid;
	}
	
	public void registerEntity(AnimatedEntity<?> animatedEntity)
	{
		animatedEntity.setModId(modid);
		AnimatedEntityRegistry.INSTANCE.registerEntity(animatedEntity);
	}
	
}
