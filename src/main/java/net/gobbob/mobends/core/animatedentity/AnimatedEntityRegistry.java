package net.gobbob.mobends.core.animatedentity;

import net.gobbob.mobends.core.Core;
import net.gobbob.mobends.core.configuration.CoreClientConfig;
import net.minecraft.entity.EntityLivingBase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AnimatedEntityRegistry
{

    public static final AnimatedEntityRegistry instance = new AnimatedEntityRegistry();

    private final Map<Class<? extends EntityLivingBase>, AnimatedEntity<?>> entityClassToInstanceMap = new HashMap<>();
    private final Map<EntityLivingBase, AnimatedEntity<?>> entityToAnimationEntityMap = new HashMap<>();

    public void registerEntity(AnimatedEntity<?> animatedEntity)
    {
        Core.LOG.info(String.format("Registering %s", animatedEntity.getKey()));
        entityClassToInstanceMap.put(animatedEntity.entityClass, animatedEntity);
    }

    public void applyConfiguration(CoreClientConfig config)
    {
        for (AnimatedEntity<?> animatedEntity : entityClassToInstanceMap.values())
        {
            animatedEntity.setAnimate(config.isEntityAnimated(animatedEntity.getKey()));
        }
    }

    public Collection<AnimatedEntity<?>> getRegistered()
    {
        return entityClassToInstanceMap.values();
    }

    public AnimatedEntity<?> getForEntityClass(Class<? extends EntityLivingBase> c)
    {
        return entityClassToInstanceMap.get(c);
    }

    public <E extends EntityLivingBase> AnimatedEntity<E> getForEntity(E entity)
    {
        if (entityToAnimationEntityMap.containsKey(entity))
            return (AnimatedEntity<E>) entityToAnimationEntityMap.get(entity);

        // Checking direct registration
        Class<? extends EntityLivingBase> entityClass = entity.getClass();
        for (AnimatedEntity<?> animatedEntity : entityClassToInstanceMap.values())
        {
            if (animatedEntity.entityClass.equals(entityClass))
            {
                entityToAnimationEntityMap.put(entity, animatedEntity);
                return (AnimatedEntity<E>) animatedEntity;
            }
        }

        // Checking indirect inheritance
        for (AnimatedEntity<?> animatedEntity : entityClassToInstanceMap.values())
        {
            if (animatedEntity.entityClass.isInstance(entity))
            {
                entityToAnimationEntityMap.put(entity, animatedEntity);
                return (AnimatedEntity<E>) animatedEntity;
            }
        }

        return null;
    }

    public void clearCache(EntityLivingBase entity)
    {
        entityClassToInstanceMap.remove(entity);
    }

    /**
     * Will clear any associations between entities and AnimatedEntities.
     * This is usually called whenever the player joins a new world.
     */
    public void clearCache()
    {
        entityClassToInstanceMap.clear();
    }

    public void refreshMutators()
    {
        for (AnimatedEntity<?> animatedEntity : entityClassToInstanceMap.values())
            animatedEntity.refreshMutation();
    }

}
