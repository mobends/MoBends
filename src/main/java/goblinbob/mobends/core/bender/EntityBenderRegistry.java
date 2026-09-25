package goblinbob.mobends.core.bender;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.types.EntityType;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This class is responsible for keeping track of all entity benders, and of which one (and which
 * type, see {@link EntityTypeRegistry}) each entity is animated by.
 */
public class EntityBenderRegistry
{

    public static final EntityBenderRegistry instance = new EntityBenderRegistry();

    /** Every bender by key, in registration order. */
    private final Map<String, EntityBender<?>> benders = new LinkedHashMap<>();

    /** The benders registered by addons: each is the default model of its entity class. */
    private final List<EntityBender<?>> defaultBenders = new ArrayList<>();

    private final Map<Class<?>, Optional<EntityBender<?>>> defaultBenderByClass = new HashMap<>();

    /**
     * Used to cache entity-to-type relationships, so they won't be calculated every time.
     * Weak keys, so entities that are no longer referenced elsewhere don't stay in memory.
     */
    private final Map<EntityLivingBase, EntityTypeRegistry.Candidates> entityToCandidatesMap = new WeakHashMap<>();

    /** Registers a bender of an addon: the default model of every entity of its class. */
    public void registerBender(EntityBender<?> entityBender)
    {
        Core.LOG.info(String.format("Registering %s", entityBender.getKey()));
        EntityBender<?> previous = benders.put(entityBender.getKey(), entityBender);
        if (previous != null)
        {
            defaultBenders.remove(previous);
        }
        defaultBenders.add(entityBender);
        defaultBenderByClass.clear();
    }

    /** Registers a bender a type made from a model definition. It only animates entities of that type. */
    public void registerTypeBender(EntityBender<?> entityBender, CoreClientConfig config)
    {
        benders.put(entityBender.getKey(), entityBender);
        if (config != null)
        {
            entityBender.setAnimate(config.isEntityAnimated(entityBender.getKey()));
        }
    }

    /** Removes the benders made by types, putting their renderers back to vanilla. */
    public void removeTypeBenders()
    {
        Iterator<EntityBender<?>> it = benders.values().iterator();
        while (it.hasNext())
        {
            EntityBender<?> bender = it.next();
            if (!defaultBenders.contains(bender))
            {
                bender.refreshMutation();
                it.remove();
            }
        }
    }

    public void applyConfiguration(CoreClientConfig config)
    {
        for (EntityBender<?> entityBender : benders.values())
        {
            entityBender.setAnimate(config.isEntityAnimated(entityBender.getKey()));
        }
    }

    public Collection<EntityBender<?>> getRegistered()
    {
        return benders.values();
    }

    public Collection<EntityBender<?>> getDefaultBenders()
    {
        return defaultBenders;
    }

    @Nullable
    public EntityBender<?> getByKey(String key)
    {
        return benders.get(key);
    }

    public Collection<EntityBender<?>> getRegistered(Filter filter)
    {
        List<EntityBender<?>> benderList = new ArrayList<>(benders.values());

        if (filter.query != null)
        {
            benderList.removeIf(bender -> !bender.getUnlocalizedName().toLowerCase().contains(filter.query.toLowerCase()));
        }

        benderList.sort(Comparator.comparing(EntityBender::getKey));

        return benderList;
    }

    /**
     * The model an entity of this class has by default: the addon bender registered for exactly this
     * class, or else the first one registered for a superclass.
     */
    @Nullable
    public EntityBender<?> getDefaultBender(Class<?> entityClass)
    {
        return defaultBenderByClass.computeIfAbsent(entityClass, c -> {
            for (EntityBender<?> entityBender : defaultBenders)
                if (entityBender.entityClass.equals(c))
                    return Optional.of(entityBender);

            for (EntityBender<?> entityBender : defaultBenders)
                if (entityBender.entityClass.isAssignableFrom(c))
                    return Optional.of(entityBender);

            return Optional.empty();
        }).orElse(null);
    }

    /** The type the entity is animated by, with its bender, or null if it stays vanilla. */
    @Nullable
    public EntityTypeRegistry.Selection getSelection(EntityLivingBase entity)
    {
        EntityTypeRegistry.Candidates candidates = entityToCandidatesMap.get(entity);
        if (candidates == null)
        {
            // Checking the config blacklist
            candidates = ModConfig.shouldKeepEntityAsVanilla(entity)
                    ? EntityTypeRegistry.Candidates.NONE
                    : EntityTypeRegistry.INSTANCE.candidatesFor(entity);
            entityToCandidatesMap.put(entity, candidates);
        }
        return candidates.select(entity);
    }

    @Nullable
    public <E extends EntityLivingBase> EntityBender<E> getForEntity(E entity)
    {
        EntityTypeRegistry.Selection selection = getSelection(entity);
        // noinspection unchecked
        return selection == null ? null : (EntityBender<E>) selection.bender;
    }

    @Nullable
    public EntityType getTypeForEntity(EntityLivingBase entity)
    {
        EntityTypeRegistry.Selection selection = getSelection(entity);
        return selection == null ? null : selection.type;
    }

    public <E extends EntityLivingBase> void clearCache(E entity)
    {
        entityToCandidatesMap.remove(entity);
    }

    /**
     * Will clear any associations between entities and types.
     * This is usually called whenever the player joins a new world, and when types or ranks change.
     */
    public void clearCache()
    {
        entityToCandidatesMap.clear();
    }

    public void refreshMutators()
    {
        clearCache();

        for (EntityBender<?> entityBender : benders.values())
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
