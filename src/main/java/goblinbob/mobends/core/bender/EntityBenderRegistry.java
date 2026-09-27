package goblinbob.mobends.core.bender;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Predicate;

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
     * The types each entity can have, so they aren't worked out every frame. Weak keys, so entities
     * that are no longer referenced elsewhere don't stay in memory. Entities compare by id, and a
     * new entity can reuse the id of one that is gone: the entry remembers which one it is for.
     */
    private final Map<EntityLivingBase, CachedCandidates> entityToCandidatesMap = new WeakHashMap<>();

    /** Entities the player chose to keep vanilla (see the mod's config). */
    private Predicate<EntityLivingBase> keepVanilla = entity -> false;

    private static final class CachedCandidates
    {
        final WeakReference<EntityLivingBase> entity;
        final EntityTypeRegistry.Candidates candidates;

        CachedCandidates(EntityLivingBase entity, EntityTypeRegistry.Candidates candidates)
        {
            this.entity = new WeakReference<>(entity);
            this.candidates = candidates;
        }
    }

    public void setKeepVanilla(Predicate<EntityLivingBase> keepVanilla)
    {
        this.keepVanilla = keepVanilla;
        clearCache();
    }

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
            entityBender.setAnimate(config.isEntityAnimated(entityBender));
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
            entityBender.setAnimate(config.isEntityAnimated(entityBender));
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

    /** The benders whose displayed name contains {@code query} (all of them for null), by key. */
    public List<EntityBender<?>> search(@Nullable String query)
    {
        List<EntityBender<?>> benderList = new ArrayList<>(benders.values());
        if (query != null)
        {
            String lowerCase = query.toLowerCase(Locale.ROOT);
            benderList.removeIf(bender -> !bender.getLocalizedName().toLowerCase(Locale.ROOT).contains(lowerCase));
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
        CachedCandidates cached = entityToCandidatesMap.get(entity);
        if (cached == null || cached.entity.get() != entity)
        {
            EntityTypeRegistry.Candidates candidates = keepVanilla.test(entity)
                    ? EntityTypeRegistry.Candidates.NONE
                    : EntityTypeRegistry.INSTANCE.candidatesFor(entity);
            cached = new CachedCandidates(entity, candidates);
            entityToCandidatesMap.remove(entity);
            entityToCandidatesMap.put(entity, cached);
        }
        return cached.candidates.select(entity);
    }

    @Nullable
    public <E extends EntityLivingBase> EntityBender<E> getForEntity(E entity)
    {
        EntityTypeRegistry.Selection selection = getSelection(entity);
        // noinspection unchecked
        return selection == null ? null : (EntityBender<E>) selection.bender;
    }

    public <E extends EntityLivingBase> void clearCache(E entity)
    {
        entityToCandidatesMap.remove(entity);
    }

    /** Forgets which types the entities have: on joining a world, and when types or ranks change. */
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

}
