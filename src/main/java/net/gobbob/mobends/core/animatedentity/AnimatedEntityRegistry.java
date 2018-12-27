package net.gobbob.mobends.core.animatedentity;

import java.util.HashMap;

import net.gobbob.mobends.core.util.BendsLogger;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.config.Configuration;

public class AnimatedEntityRegistry
{
	public static AnimatedEntityRegistry INSTANCE = new AnimatedEntityRegistry();
	
	private HashMap<String, AnimatedEntity<?>> animatedEntities = new HashMap<String, AnimatedEntity<?>>();

	public void registerEntity(AnimatedEntity<?> animatedEntity)
	{
		BendsLogger.info("Registering " + animatedEntity.name);
		animatedEntities.put(animatedEntity.name, animatedEntity);
	}
	
	public static void applyConfiguration(Configuration config)
	{
		for (AnimatedEntity<?> e : INSTANCE.animatedEntities.values())
		{
			for (AlterEntry alterEntry : e.alterEntries)
			{
				alterEntry.setAnimate(config.get("Animated", alterEntry.getName(), true).getBoolean());
			}
		}
	}
	
	public static Iterable<AnimatedEntity<?>> getRegistered()
	{
		return INSTANCE.animatedEntities.values();
	}
	
	public static AnimatedEntity get(String name)
	{
		return INSTANCE.animatedEntities.get(name);
	}
	
	public static AnimatedEntity getForEntity(Entity entity)
	{
		// Checking direct registration
		Class<? extends Entity> entityClass = entity.getClass();
		for (AnimatedEntity animatedEntity : INSTANCE.animatedEntities.values())
			if (animatedEntity.entityClass.equals(entityClass))
				return animatedEntity;

		// Checking indirect inheritance
		for (AnimatedEntity animatedEntity : INSTANCE.animatedEntities.values())
			if (animatedEntity.entityClass.isInstance(entity))
				return animatedEntity;

		return null;
	}
	
	public static void refreshMutators()
	{
		for (AnimatedEntity animatedEntity : INSTANCE.animatedEntities.values())
			animatedEntity.refreshMutation();
	}
}
