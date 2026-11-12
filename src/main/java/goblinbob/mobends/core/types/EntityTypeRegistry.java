package goblinbob.mobends.core.types;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.AnimationPolicy;
import goblinbob.mobends.core.client.definition.DefinedEntities.DefinedEntity;
import goblinbob.mobends.core.client.definition.DefinedEntities;
import goblinbob.mobends.core.configuration.ClientConfig;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.network.ResourcePackPolicy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.Nullable;

/**
 * Every entity type, one for each type file found in {@code assets/<namespace>/bends/types/} of
 * every mod and resource pack, Mo' Bends' own mobs included. Decides which type animates an entity
 * (see {@code misc/kumo-format.md}, "Entity types and selectors"), and gives each type its
 * extensions ({@code bends/extensions/}).
 * <p>
 * Loaded when first asked after a resource reload, as the definitions and animators are. The
 * Mo' Bends menu lists the types and extensions of each animated mob ({@link #getTypesFor},
 * {@link #getExtensionsFor}) and orders them (the user's ranks, kept in {@code ClientConfig}).
 */
public final class EntityTypeRegistry {
    public static final EntityTypeRegistry INSTANCE = new EntityTypeRegistry();

    private final Map<String, EntityType> types = new LinkedHashMap<>();
    private final Map<String, Extension> extensions = new LinkedHashMap<>();
    private boolean loaded;

    /**
     * The types each entity can have, so they aren't worked out every frame. Weak keys, so entities
     * that are no longer referenced elsewhere don't stay in memory. Entities compare by id, and a
     * new entity can reuse the id of one that is gone: the entry remembers which one it is for.
     */
    private final Map<LivingEntity, CachedCandidates> candidatesByEntity = new WeakHashMap<>();

    private record CachedCandidates(WeakReference<LivingEntity> entity, Candidates candidates) {
    }

    /** What a type does to an entity: its model (null: vanilla) and the factory of the entity's data. */
    public record Selection(EntityType type, @Nullable DefinedEntity model, @Nullable IEntityDataFactory<LivingEntity> dataFactory) {
    }

    /**
     * The types that can apply to one entity: the stable ones whose selector held (decided once),
     * and the unstable ones, asked again every frame.
     */
    private static final class Candidates {
        static final Candidates NONE = new Candidates(Collections.emptyList(), Collections.emptyList());

        private final List<EntityType> unstable;
        /** The first of the stable types whose selector held. */
        @Nullable
        private final EntityType bestStable;

        Candidates(List<EntityType> matched, List<EntityType> unstable) {
            this.unstable = unstable;
            this.bestStable = TypeOrder.first(matched);
        }

        @Nullable
        Selection select(LivingEntity entity) {
            EntityType winner = bestStable;
            for (EntityType type : unstable) {
                if (type.matches(entity) && (winner == null || TypeOrder.PRECEDENCE.compare(type, winner) < 0)) {
                    winner = type;
                }
            }
            if (winner == null) {
                return null;
            }
            Selection selection = winner.selectionFor(entity);
            return selection == null || selection.model() == null ? null : selection;
        }
    }

    private EntityTypeRegistry() {
    }

    /** The type the entity is animated by, with its model, or null if it stays vanilla. */
    @Nullable
    public Selection select(LivingEntity entity) {
        ensureLoaded();
        CachedCandidates cached = candidatesByEntity.get(entity);
        if (cached == null || cached.entity().get() != entity) {
            cached = new CachedCandidates(new WeakReference<>(entity), candidatesFor(entity));
            candidatesByEntity.remove(entity);
            candidatesByEntity.put(entity, cached);
        }
        return cached.candidates().select(entity);
    }

    private Candidates candidatesFor(LivingEntity entity) {
        List<EntityType> matched = new ArrayList<>();
        List<EntityType> unstable = new ArrayList<>();
        for (EntityType type : types.values()) {
            if (type.selectionFor(entity) == null) {
                continue;
            }
            if (!type.isStable()) {
                unstable.add(type);
            } else if (type.matches(entity)) {
                matched.add(type);
            }
        }
        return matched.isEmpty() && unstable.isEmpty() ? Candidates.NONE : new Candidates(matched, unstable);
    }

    /** The model with this key ({@code mobends:player}), or null if no type file brought one. */
    @Nullable
    public DefinedEntity modelByKey(String key) {
        ensureLoaded();
        return DefinedEntities.byKey(key);
    }

    /**
     * Every mob Mo' Bends animates now, as the Mo' Bends menu lists them: those the type files
     * brought, each once; not those whose definition can't be loaded.
     */
    public List<DefinedEntity> getDefinedEntities() {
        ensureLoaded();
        List<DefinedEntity> found = new ArrayList<>();
        for (DefinedEntity defined : DefinedEntities.all()) {
            if (defined.key() != null && !found.contains(defined)) {
                found.add(defined);
            }
        }
        return found;
    }

    /**
     * The types that can animate the entities of {@code defined} with it, in precedence order: those
     * that give that model, and those that keep entities vanilla where they could get it: for an
     * entity type one of its types names, or for any entity, if they name no entity type.
     */
    public List<EntityType> getTypesFor(DefinedEntity defined) {
        ensureLoaded();
        List<EntityType> found = new ArrayList<>();
        Set<ResourceLocation> entityTypes = new HashSet<>();
        for (EntityType type : types.values()) {
            if (type.getModel() == defined) {
                found.add(type);
                entityTypes.addAll(type.getEntityTypes());
            }
        }
        for (EntityType type : types.values()) {
            if (!type.isVanilla()) {
                continue;
            }
            Collection<ResourceLocation> named = type.getEntityTypes();
            if (named.isEmpty() || !Collections.disjoint(named, entityTypes)) {
                found.add(type);
            }
        }
        found.sort(TypeOrder.PRECEDENCE);
        return found;
    }

    /**
     * The entity types {@code defined} animates, as the Mo' Bends menu switches them on and off, in
     * its definition's order: those its definition names that a type giving that model can give it
     * to (any of them, for a type whose selector names no entity type). Empty if no type gives it.
     */
    public List<ResourceLocation> getEntityTypesOf(DefinedEntity defined) {
        ensureLoaded();
        Set<ResourceLocation> given = new HashSet<>();
        boolean any = false;
        for (EntityType type : types.values()) {
            if (type.getModel() == defined) {
                Collection<ResourceLocation> named = type.getEntityTypes();
                if (named.isEmpty()) {
                    any = true;
                }
                given.addAll(named);
            }
        }
        List<ResourceLocation> found = new ArrayList<>();
        for (ResourceLocation id : defined.entityTypes()) {
            if (any || given.contains(id)) {
                found.add(id);
            }
        }
        return found;
    }

    /** The extensions of the types that can animate {@code defined}'s entities, in precedence order (the first goes on top). */
    public List<Extension> getExtensionsFor(DefinedEntity defined) {
        Set<String> typeIds = new HashSet<>();
        for (EntityType type : getTypesFor(defined)) {
            typeIds.add(type.getId());
        }
        List<Extension> found = new ArrayList<>();
        for (Extension extension : extensions.values()) {
            if (typeIds.contains(extension.getTypeId())) {
                found.add(extension);
            }
        }
        found.sort(TypeOrder.PRECEDENCE);
        return found;
    }

    /**
     * What animates a preview of {@code defined} (the Mo' Bends menu's): of the types that can give
     * the entity that model (see {@link #getTypesFor}), the first whose selector holds for it, or
     * else the first that can apply to it at all; whatever type would win for it in the game. Null
     * if none gives it that model.
     */
    @Nullable
    public Selection selectionAs(DefinedEntity defined, LivingEntity entity) {
        Selection fallback = null;
        for (EntityType type : getTypesFor(defined)) {
            Selection selection = type.selectionFor(entity);
            if (selection == null || selection.model() != defined) {
                continue;
            }
            if (type.matches(entity)) {
                return selection;
            }
            if (fallback == null) {
                fallback = selection;
            }
        }
        return fallback;
    }

    /** Ranks the types so {@code order} holds (the first gets precedence), and stores the ranks. */
    public void setOrder(List<EntityType> order) {
        TypeOrder.rankInOrder(order, (type, rank) -> ClientConfig.INSTANCE.setTypeRank(type.getId(), rank));
        onRanksChanged();
    }

    /** Puts the types back to rank 0, so specificity and ids decide again. */
    public void resetRanks(List<EntityType> order) {
        for (EntityType type : order) {
            ClientConfig.INSTANCE.setTypeRank(type.getId(), 0);
        }
        onRanksChanged();
    }

    /** Ranks the extensions so {@code order} holds (the first one highest, so on top), and stores the ranks. */
    public void setExtensionOrder(List<Extension> order) {
        TypeOrder.rankInOrder(order, (extension, rank) -> ClientConfig.INSTANCE.setExtensionRank(extension.getId(), rank));
        applyExtensions();
        onRanksChanged();
    }

    /** Puts the extensions back to rank 0, so their ids decide the order again. */
    public void resetExtensionRanks(List<Extension> order) {
        for (Extension extension : order) {
            ClientConfig.INSTANCE.setExtensionRank(extension.getId(), 0);
        }
        applyExtensions();
        onRanksChanged();
    }

    /** The order changed: which type wins is decided again, and every entity's data is made anew. */
    private void onRanksChanged() {
        candidatesByEntity.clear();
        EntityDatabase.instance.refresh();
    }

    /** The types no longer match the resources (after a reload): they load again on next use. */
    public void markStale() {
        loaded = false;
        types.clear();
        extensions.clear();
        candidatesByEntity.clear();
    }

    private void ensureLoaded() {
        if (!loaded) {
            reload();
        }
    }

    /**
     * (Re)loads every type, then the extensions. The type files are read first, every model one
     * names by path made, and only then are the types built: a type naming a model by key finds it
     * whichever type file named its definition, before or after it.
     */
    private void reload() {
        loaded = true;
        types.clear();
        candidatesByEntity.clear();

        ResourcePackPolicy policy = AnimationPolicy.INSTANCE.current();
        List<TypeFiles.Read> read = new ArrayList<>();
        for (TypeFileDiscovery.TypeFile file : TypeFileDiscovery.discover("types")) {
            if (!file.trusted() && policy == ResourcePackPolicy.DENY) {
                MoBends.LOGGER.info("Mo' Bends: the server denies resource packs' animation: ignoring the type {}", file.source());
                continue;
            }
            try {
                TypeFiles.Read type = TypeFiles.read(file, policy);
                if (type != null) {
                    read.add(type);
                }
            } catch (Exception | StackOverflowError e) {
                MoBends.LOGGER.error("Mo' Bends: could not load the type {}: {}", file.source(), e.getMessage(), e);
            }
        }
        for (TypeFiles.Read type : read) {
            try {
                add(TypeFiles.build(type));
            } catch (Exception | StackOverflowError e) {
                MoBends.LOGGER.error("Mo' Bends: could not load the type {}: {}", type.file().source(), e.getMessage(), e);
            }
        }
        int extensionCount = loadExtensions();
        MoBends.LOGGER.info("Mo' Bends: loaded {} entity types and {} extensions", types.size(), extensionCount);
        migrateLegacySwitches();
    }

    /** The switches of 2.0.0-alpha.4 and before (per model, and keepEntityAsVanilla) become switches per entity type. */
    private void migrateLegacySwitches() {
        List<String> registered = new ArrayList<>();
        for (ResourceLocation id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            registered.add(id.toString());
        }
        ClientConfig.INSTANCE.migrateLegacySwitches(registered, key -> {
            DefinedEntity defined = DefinedEntities.byKey(key);
            List<String> entityTypes = new ArrayList<>();
            if (defined != null) {
                for (ResourceLocation id : getEntityTypesOf(defined)) {
                    entityTypes.add(id.toString());
                }
            }
            return entityTypes;
        });
    }

    /** Loads every extension and gives each type its own; returns how many there are. */
    private int loadExtensions() {
        extensions.clear();
        ResourcePackPolicy policy = AnimationPolicy.INSTANCE.current();
        for (TypeFileDiscovery.TypeFile file : TypeFileDiscovery.discover("extensions")) {
            if (!file.trusted() && policy == ResourcePackPolicy.DENY) {
                MoBends.LOGGER.info("Mo' Bends: the server denies resource packs' animation: ignoring the extension {}", file.source());
                continue;
            }
            try {
                Extension extension = TypeFiles.loadExtension(file);
                if (!types.containsKey(extension.getTypeId())) {
                    MoBends.LOGGER.warn("Mo' Bends: the extension '{}' ({}) extends the type '{}', which doesn't exist.",
                            extension.getId(), file.source(), extension.getTypeId());
                    continue;
                }
                Extension overridden = extensions.put(extension.getId(), extension);
                if (overridden != null) {
                    MoBends.LOGGER.warn("Mo' Bends: two extensions have the id '{}': using {}, ignoring {}",
                            extension.getId(), extension.getSource(), overridden.getSource());
                }
            } catch (Exception | StackOverflowError e) {
                MoBends.LOGGER.error("Mo' Bends: could not load the extension {}: {}", file.source(), e.getMessage(), e);
            }
        }
        applyExtensions();
        return extensions.size();
    }

    /** Gives each type its extensions, in the order their layers go on (the user's ranks, then ids). */
    private void applyExtensions() {
        for (EntityType type : types.values()) {
            List<Extension> own = new ArrayList<>();
            for (Extension extension : extensions.values()) {
                if (extension.getTypeId().equals(type.getId())) {
                    own.add(extension);
                }
            }
            type.setExtensions(Extension.inLayerOrder(own));
        }
    }

    /** Adds a type; one found later (in a higher-priority pack) replaces one with the same id. */
    private void add(EntityType type) {
        EntityType overridden = types.put(type.getId(), type);
        if (overridden != null) {
            MoBends.LOGGER.warn("Mo' Bends: two types have the id '{}': using {}, ignoring {}", type.getId(), type.getSource(), overridden.getSource());
        }
    }
}
