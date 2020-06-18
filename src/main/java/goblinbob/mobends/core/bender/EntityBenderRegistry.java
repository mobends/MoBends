package goblinbob.mobends.core.bender;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import net.minecraft.entity.EntityLivingBase;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is responsible for keeping track of all entity benders.
 */
public class EntityBenderRegistry
{

    public static final EntityBenderRegistry instance = new EntityBenderRegistry();

    private final Map<Class<? extends EntityLivingBase>, EntityBender<?>> entityClassToBenderMap = new HashMap<>();

    /**
     * Used to cache entity-to-bender relationships, so they won't be calculated every time.
     */
    private final Map<EntityLivingBase, EntityBender<?>> entityToBenderMap = new HashMap<>();

    public void registerBender(EntityBender<?> entityBender)
    {
        Core.LOG.info(String.format("Registering %s", entityBender.getKey()));
        entityClassToBenderMap.put(entityBender.entityClass, entityBender);
    }

    public void applyConfiguration(CoreClientConfig config)
    {
        for (EntityBender<?> entityBender : entityClassToBenderMap.values())
        {
            entityBender.setAnimate(config.isEntityAnimated(entityBender.getKey()));
        }
    }

    public Collection<EntityBender<?>> getRegistered()
    {
        return entityClassToBenderMap.values();
    }

    public Collection<EntityBender<?>> getRegistered(Filter filter)
    {
        List<EntityBender<?>> benderList = new ArrayList<>(entityClassToBenderMap.values());

        if (filter.query != null)
        {
            benderList.removeIf(bender -> !bender.getUnlocalizedName().toLowerCase().contains(filter.query.toLowerCase()));
        }

        benderList.sort(Comparator.comparing(EntityBender::getKey));

        return benderList;
    }

    public <E extends EntityLivingBase> EntityBender<E> getForEntityClass(Class<E> c)
    {
        // noinspection unchecked
        return (EntityBender<E>) entityClassToBenderMap.get(c);
    }

    public <E extends EntityLivingBase> EntityBender<E> getForEntity(E entity)
    {
        if (entityToBenderMap.containsKey(entity))
            // noinspection unchecked
            return (EntityBender<E>) entityToBenderMap.get(entity);

        // Checking direct registration
        Class<? extends EntityLivingBase> entityClass = entity.getClass();
        for (EntityBender<?> entityBender : entityClassToBenderMap.values())
        {
            if (entityBender.entityClass.equals(entityClass))
            {
                entityToBenderMap.put(entity, entityBender);
                // noinspection unchecked
                return (EntityBender<E>) entityBender;
            }
        }

        // Checking indirect inheritance
        for (EntityBender<?> entityBender : entityClassToBenderMap.values())
        {
            if (entityBender.entityClass.isInstance(entity))
            {
                entityToBenderMap.put(entity, entityBender);
                // noinspection unchecked
                return (EntityBender<E>) entityBender;
            }
        }

        return null;
    }

    public <E extends EntityLivingBase> void clearCache(E entity)
    {
        entityToBenderMap.remove(entity);
    }

    /**
     * Will clear any associations between entities and EntityBenders.
     * This is usually called whenever the player joins a new world.
     */
    public void clearCache()
    {
        entityToBenderMap.clear();
    }

    public void refreshMutators()
    {
        for (EntityBender<?> entityBender : entityClassToBenderMap.values())
            entityBender.refreshMutation();
    }

    public static class Filter
    {
        public boolean ascending = false;
        public SortingKey sortingKey = SortingKey.NAME;
        public String query = null;

        public enum SortingKey
        {
            NAME,
        }
    }

}
