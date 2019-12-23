package net.gobbob.mobends.core.bender;

import net.gobbob.mobends.core.Core;
import net.gobbob.mobends.core.configuration.CoreClientConfig;
import net.minecraft.entity.EntityLivingBase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EntityBenderRegistry
{

    public static final EntityBenderRegistry instance = new EntityBenderRegistry();

    private final Map<Class<? extends EntityLivingBase>, EntityBender<?>> entityClassToInstanceMap = new HashMap<>();
    private final Map<EntityLivingBase, EntityBender<?>> entityToBenderMap = new HashMap<>();

    public void registerEntity(EntityBender<?> entityBender)
    {
        Core.LOG.info(String.format("Registering %s", entityBender.getKey()));
        entityClassToInstanceMap.put(entityBender.entityClass, entityBender);
    }

    public void applyConfiguration(CoreClientConfig config)
    {
        for (EntityBender<?> entityBender : entityClassToInstanceMap.values())
        {
            entityBender.setAnimate(config.isEntityAnimated(entityBender.getKey()));
        }
    }

    public Collection<EntityBender<?>> getRegistered()
    {
        return entityClassToInstanceMap.values();
    }

    public EntityBender<?> getForEntityClass(Class<? extends EntityLivingBase> c)
    {
        return entityClassToInstanceMap.get(c);
    }

    public <E extends EntityLivingBase> EntityBender<E> getForEntity(E entity)
    {
        if (entityToBenderMap.containsKey(entity))
            return (EntityBender<E>) entityToBenderMap.get(entity);

        // Checking direct registration
        Class<? extends EntityLivingBase> entityClass = entity.getClass();
        for (EntityBender<?> entityBender : entityClassToInstanceMap.values())
        {
            if (entityBender.entityClass.equals(entityClass))
            {
                entityToBenderMap.put(entity, entityBender);
                return (EntityBender<E>) entityBender;
            }
        }

        // Checking indirect inheritance
        for (EntityBender<?> entityBender : entityClassToInstanceMap.values())
        {
            if (entityBender.entityClass.isInstance(entity))
            {
                entityToBenderMap.put(entity, entityBender);
                return (EntityBender<E>) entityBender;
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
        for (EntityBender<?> entityBender : entityClassToInstanceMap.values())
            entityBender.refreshMutation();
    }

}
