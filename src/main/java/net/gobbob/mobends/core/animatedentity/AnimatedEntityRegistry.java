package net.gobbob.mobends.core.animatedentity;

import java.util.HashMap;

import net.gobbob.mobends.core.client.MutatedRenderer;
import net.gobbob.mobends.core.mutators.IMutatorFactory;
import net.gobbob.mobends.core.util.BendsLogger;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

public class AnimatedEntityRegistry
{
	public static AnimatedEntityRegistry INSTANCE = new AnimatedEntityRegistry();
	
	private HashMap<String, AnimatedEntity<?>> nameToInstanceMap = new HashMap<String, AnimatedEntity<?>>();
	private HashMap<Class<? extends Entity>, AnimatedEntity<?>> entityClassToInstanceMap = new HashMap<Class<? extends Entity>, AnimatedEntity<?>>();
	
	
	public void registerEntity(AnimatedEntity<?> animatedEntity)
	{
		BendsLogger.LOGGER.info("Registering " + animatedEntity.key);
		
		if (animatedEntity.onRegistraton())
		{
			nameToInstanceMap.put(animatedEntity.key, animatedEntity);
			entityClassToInstanceMap.put(animatedEntity.entityClass, animatedEntity);
		}
	}
	
	public static void applyConfiguration(Configuration config)
	{
		for (AnimatedEntity<?> e : INSTANCE.nameToInstanceMap.values())
		{
			for (AlterEntry alterEntry : e.alterEntries)
			{
				alterEntry.setAnimate(config.get("Animated", alterEntry.getKey(), true).getBoolean());
			}
		}
	}
	
	public static Iterable<AnimatedEntity<?>> getRegistered()
	{
		return INSTANCE.nameToInstanceMap.values();
	}
	
	public static AnimatedEntity get(String name)
	{
		return INSTANCE.nameToInstanceMap.get(name);
	}
	
	public static AnimatedEntity getForEntityClass(Class<? extends Entity> c)
	{
		return INSTANCE.entityClassToInstanceMap.get(c);
	}
	
	public static AnimatedEntity getForEntity(Entity entity)
	{
		// Checking direct registration
		Class<? extends Entity> entityClass = entity.getClass();
		for (AnimatedEntity animatedEntity : INSTANCE.nameToInstanceMap.values())
			if (animatedEntity.entityClass.equals(entityClass))
				return animatedEntity;

		// Checking indirect inheritance
		for (AnimatedEntity animatedEntity : INSTANCE.nameToInstanceMap.values())
			if (animatedEntity.entityClass.isInstance(entity))
				return animatedEntity;

		return null;
	}
	
	public static void refreshMutators()
	{
		for (AnimatedEntity animatedEntity : INSTANCE.nameToInstanceMap.values())
			animatedEntity.refreshMutation();
	}
}
