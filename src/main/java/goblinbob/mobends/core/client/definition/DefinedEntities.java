package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.vanilla.VanillaNames;
import net.minecraft.client.model.EntityModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The entities Mo' Bends animates, each made from a model definition (1.12's benders): those
 * registered in code ({@link #register}), each the default model of its entity class, and those
 * type files name ({@link #forTypeFile}), which animate only the entities of their types. Which
 * one an entity gets is its type's choice ({@code core.types.EntityTypeRegistry}).
 */
public final class DefinedEntities {
    /** Registered in code, in order: the default models of their entity classes. */
    private static final List<DefinedEntity> DEFAULTS = new ArrayList<>();
    /** Made for the type files since the last reload. */
    private static final List<DefinedEntity> FROM_TYPES = new ArrayList<>();
    /** The default model by entity type id and class ({@link #defaultFor(ResourceLocation, Class)}). */
    private static final Map<List<Object>, Optional<DefinedEntity>> DEFAULT_BY_TYPE = new HashMap<>();

    private DefinedEntities() {
    }

    /** Animates the entities {@code <modId>:bends/models/<name>.json} defines, as the default model of its entity class. */
    public static void register(String modId, String name) {
        DEFAULTS.add(new DefinedEntity(modId, ModelDefinitions.locationOf(modId, name)));
    }

    /** The entities registered in code. */
    public static List<DefinedEntity> defaults() {
        return DEFAULTS;
    }

    /** Every defined entity: those registered in code, then those made for the type files since the last reload. */
    public static List<DefinedEntity> all() {
        List<DefinedEntity> all = new ArrayList<>(DEFAULTS);
        all.addAll(FROM_TYPES);
        return all;
    }

    /**
     * The defined entity of the model definition at {@code location}, which a type file names:
     * the one already made under the same key (registered in code, or for another type), else a
     * new one, which lives until the next reload.
     *
     * @throws Exception if the definition can't be loaded, or names no entity this version knows
     */
    public static DefinedEntity forTypeFile(ResourceLocation location) throws Exception {
        DefinedEntity made = new DefinedEntity(location.getNamespace(), location);
        made.loadOrThrow();
        DefinedEntity existing = byKey(made.key());
        if (existing != null) {
            return existing;
        }
        FROM_TYPES.add(made);
        return made;
    }

    /** The defined entity with this key ({@code mobends:zombie}), or null if none (that loads) has it. */
    @Nullable
    public static DefinedEntity byKey(String key) {
        for (List<DefinedEntity> list : List.of(DEFAULTS, FROM_TYPES)) {
            for (DefinedEntity defined : list) {
                if (key.equals(defined.key())) {
                    return defined;
                }
            }
        }
        return null;
    }

    /** The model {@code entity} has by default ({@link #defaultFor(ResourceLocation, Class)}). */
    @Nullable
    public static DefinedEntity defaultFor(LivingEntity entity) {
        return defaultFor(typeIdOf(entity), entity.getClass());
    }

    /**
     * The model an entity of the type {@code typeId} and the class {@code entityClass} (null if not
     * known) has by default, of those registered in code: the first that names its type by id, else
     * the one for the closest class it is an instance of (its own, else the nearest superclass), so
     * a zombie villager, a zombie, gets the zombie villager's and a husk the zombie's. 1.12 took the
     * first registered superclass instead, which picks the same for every 1.12 mob.
     */
    @Nullable
    public static DefinedEntity defaultFor(ResourceLocation typeId, @Nullable Class<?> entityClass) {
        return DEFAULT_BY_TYPE.computeIfAbsent(Arrays.asList(typeId, entityClass), k -> {
            DefinedEntity best = null;
            Class<?> bestClass = null;
            for (DefinedEntity defined : DEFAULTS) {
                defined.ensureLoaded();
                if (defined.types.contains(typeId)) {
                    return Optional.of(defined);
                }
                if (entityClass == null) {
                    continue;
                }
                for (Class<?> type : defined.classes) {
                    if (type.isAssignableFrom(entityClass) && (best == null || bestClass.isAssignableFrom(type))) {
                        best = defined;
                        bestClass = type;
                    }
                }
            }
            return Optional.ofNullable(best);
        }).orElse(null);
    }

    /** The registry id of {@code entity}'s type ({@code minecraft:fox}). */
    public static ResourceLocation typeIdOf(LivingEntity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
    }

    /** Forgets the definitions read and the entities made for type files: they're made again (after a resource reload). */
    public static void clearCache() {
        for (DefinedEntity defined : DEFAULTS) {
            defined.clearCache();
        }
        FROM_TYPES.clear();
        DEFAULT_BY_TYPE.clear();
    }

    /** An entity made from a model definition, which is read when it is first needed. */
    public static final class DefinedEntity {
        private final String modId;
        private final ResourceLocation location;
        /** Makes the entity's data from the current definition; the same factory across reloads. */
        private final IEntityDataFactory<LivingEntity> dataFactory;
        @Nullable
        private EntityModelDefinition definition;
        /** The classes its definition names that this version knows, until the next reload. */
        private List<Class<?>> classes = List.of();
        /** The entity types its definition names by id that this version knows, until the next reload. */
        private Set<ResourceLocation> types = Set.of();
        @Nullable
        private String key;
        /** Whether the definition failed to load (logged once), until the next reload. */
        private boolean failed;
        /** Whether it was said that the definition's model has no counterpart here, until the next reload. */
        private boolean warnedModel;

        private DefinedEntity(String modId, ResourceLocation location) {
            this.modId = modId;
            this.location = location;
            this.dataFactory = entity -> new DefinedEntityData<>(definition(), entity);
        }

        public ResourceLocation location() {
            return location;
        }

        /** The factory of the entity's data when its type keeps the definition's animator. */
        public IEntityDataFactory<LivingEntity> dataFactory() {
            return dataFactory;
        }

        /** The definition; only called once {@link #entityClass()} or {@link #key()} has loaded it. */
        public EntityModelDefinition definition() {
            if (definition == null) {
                throw new IllegalStateException("The model definition " + location + " isn't loaded.");
            }
            return definition;
        }

        /**
         * The first entity class the definition names by class name, or null if it names none this
         * version has (or names its entities by id alone) or can't be loaded.
         */
        @Nullable
        public Class<?> entityClass() {
            ensureLoaded();
            return classes.isEmpty() ? null : classes.get(0);
        }

        /**
         * Its key, which its built-in type takes as its id: the mod that registered it, and the
         * definition's {@code key}, or else one made from its first entity
         * ({@link EntityModelDefinition#defaultKey()}: {@code mobends:zombie_pigman},
         * {@code mobends:fox}); null if the definition can't be loaded.
         */
        @Nullable
        public String key() {
            ensureLoaded();
            return key;
        }

        /**
         * Today's id of the first entity type its definition names that this version knows: by id,
         * or by one of Minecraft's 1.12 classes ({@code minecraft:zombified_piglin}); null if there's
         * none (it names another mod's classes alone) or the definition can't be loaded.
         */
        @Nullable
        public ResourceLocation entityTypeId() {
            ensureLoaded();
            if (definition == null) {
                return null;
            }
            for (String name : definition.entity) {
                ResourceLocation id = idNamedBy(name);
                if (id != null) {
                    return id;
                }
            }
            return null;
        }

        /** Whether it can animate {@code entity}: of a type it names by id, or an instance of a class it names. */
        public boolean animates(LivingEntity entity) {
            ensureLoaded();
            if (!types.isEmpty() && types.contains(typeIdOf(entity))) {
                return true;
            }
            for (Class<?> type : classes) {
                if (type.isInstance(entity)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Whether the definition bends {@code model}, the entity's renderer's: any model if it names
         * none, else one of the class it names ({@link EntityModelDefinition#modelClass()}). A class
         * not loaded here (a 1.12 model without a counterpart on this version, or a mod's that isn't
         * installed) is said once; the entity is drawn as vanilla.
         */
        public boolean bends(EntityModel<?> model) {
            EntityModelDefinition loaded = definition();
            if (loaded.model == null) {
                return true;
            }
            Class<?> modelClass = loaded.modelClass();
            if (modelClass == null && !warnedModel) {
                warnedModel = true;
                MoBends.LOGGER.warn("Model definition {}: its model {} isn't a model class of Minecraft {} or an installed mod; the entity is drawn as vanilla.",
                        location, loaded.model, MoBends.MINECRAFT);
            }
            return modelClass != null && modelClass.isInstance(model);
        }

        private void ensureLoaded() {
            if (definition == null && !failed) {
                try {
                    loadOrThrow();
                } catch (Exception e) {
                    failed = true;
                    MoBends.LOGGER.error("Mo' Bends: could not load the model definition {}: {}", location, e.getMessage(), e);
                }
            }
        }

        private void loadOrThrow() throws Exception {
            EntityModelDefinition loaded = ModelDefinitions.INSTANCE.load(location);
            List<Class<?>> knownClasses = new ArrayList<>();
            Set<ResourceLocation> knownTypes = new HashSet<>();
            for (String name : loaded.entity) {
                if (EntityModelDefinition.isEntityId(name)) {
                    ResourceLocation id = idNamedBy(name);
                    if (id != null && BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                        knownTypes.add(id);
                    }
                } else {
                    Class<?> type = entityClassOf(name);
                    if (type != null) {
                        knownClasses.add(type);
                    }
                }
            }
            if (knownClasses.isEmpty() && knownTypes.isEmpty()) {
                throw new MalformedKumoTemplateException("its entity " + String.join(", ", loaded.entity) + " is no living entity this version knows.");
            }
            String name = loaded.key != null ? loaded.key : loaded.defaultKey();
            if (name == null) {
                throw new MalformedKumoTemplateException("it needs a 'key': its entity " + loaded.entity.get(0) + " isn't one of Minecraft's.");
            }
            definition = loaded;
            classes = List.copyOf(knownClasses);
            types = Set.copyOf(knownTypes);
            key = modId + ":" + name;
        }

        /**
         * Today's id of the entity type {@code name} (one of a definition's entities) names: an id,
         * renamed if 1.12 had it under another, or one of Minecraft's 1.12 classes; null otherwise.
         */
        @Nullable
        private static ResourceLocation idNamedBy(String name) {
            String id = EntityModelDefinition.isEntityId(name) ? name : VanillaNames.legacyEntityId(name);
            ResourceLocation location = id == null ? null : ResourceLocation.tryParse(id);
            return location == null ? null : VanillaNames.entityType(location);
        }

        /**
         * The living entity class a definition's entity names: one of Minecraft's by its 1.12 name,
         * through {@link VanillaNames}; another mod's by its own name, which loaders don't rename;
         * null if this version has none.
         */
        @Nullable
        private static Class<?> entityClassOf(String name) {
            Class<?> type = VanillaNames.entityClass(name);
            if (type == null && !name.startsWith("net.minecraft.")) {
                try {
                    // Without initialising it: a resource pack may name any class.
                    type = Class.forName(name, false, DefinedEntities.class.getClassLoader());
                } catch (ClassNotFoundException | LinkageError e) {
                    type = null;
                }
            }
            return type != null && LivingEntity.class.isAssignableFrom(type) ? type : null;
        }

        private void clearCache() {
            definition = null;
            classes = List.of();
            types = Set.of();
            key = null;
            failed = false;
            warnedModel = false;
        }
    }
}
