package net.gobbob.mobends.core.animatedentity;

import java.util.HashMap;

import net.gobbob.mobends.core.util.BendsLogger;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.config.Configuration;

public class AnimatedEntityRegistry
{
	static final AnimatedEntityRegistry INSTANCE = new AnimatedEntityRegistry();
	
	private final HashMap<Class<? extends EntityLivingBase>, AnimatedEntity<?>> entityClassToInstanceMap = new HashMap<>();
	
	
	public void registerEntity(AnimatedEntity<?> animatedEntity)
	{
		if (animatedEntity.onRegister())
		{
			BendsLogger.LOG.info("Registering " + animatedEntity.getKey());
			entityClassToInstanceMap.put(animatedEntity.entityClass, animatedEntity);
		}
	}
	
	public static void applyConfiguration(Configuration config)
	{
		for (AnimatedEntity<?> e : INSTANCE.entityClassToInstanceMap.values())
		{
			for (AlterEntry<?> alterEntry : e.getAlterEntries())
			{
				alterEntry.setAnimate(config.get("Animated", alterEntry.getKey(), true).getBoolean());
			}
		}
	}
	
	public static Iterable<AnimatedEntity<?>> getRegistered()
	{
		return INSTANCE.entityClassToInstanceMap.values();
	}
	
	public static AnimatedEntity<?> getForEntityClass(Class<? extends EntityLivingBase> c)
	{
		return INSTANCE.entityClassToInstanceMap.get(c);
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends EntityLivingBase> AnimatedEntity<E> getForEntity(E entity)
	{
		// Checking direct registration
		Class<? extends EntityLivingBase> entityClass = entity.getClass();
		for (AnimatedEntity<?> animatedEntity : INSTANCE.entityClassToInstanceMap.values())
			if (animatedEntity.entityClass.equals(entityClass))
				return (AnimatedEntity<E>) animatedEntity;

		// Checking indirect inheritance
		for (AnimatedEntity<?> animatedEntity : INSTANCE.entityClassToInstanceMap.values())
			if (animatedEntity.entityClass.isInstance(entity))
				return (AnimatedEntity<E>) animatedEntity;

		return null;
	}
	
	public static void refreshMutators()
	{
		for (AnimatedEntity<?> animatedEntity : INSTANCE.entityClassToInstanceMap.values())
			animatedEntity.refreshMutation();
	}
}
