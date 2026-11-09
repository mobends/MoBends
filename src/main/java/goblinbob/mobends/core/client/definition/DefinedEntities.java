package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.vanilla.VanillaNames;
import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private static final Map<Class<?>, Optional<DefinedEntity>> DEFAULT_BY_CLASS = new HashMap<>();

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

    /**
     * The model an entity of {@code entityClass} has by default: of those registered in code, the
     * one for the closest class it is an instance of (its own, else the nearest superclass), so a
     * zombie villager, a zombie, gets the zombie villager's and a husk the zombie's. 1.12 took the
     * first registered superclass instead, which picks the same for every 1.12 mob.
     */
    @Nullable
    public static DefinedEntity defaultFor(Class<?> entityClass) {
        return DEFAULT_BY_CLASS.computeIfAbsent(entityClass, c -> {
            DefinedEntity best = null;
            for (DefinedEntity defined : DEFAULTS) {
                Class<?> type = defined.entityClass();
                if (type != null && type.isAssignableFrom(c) && (best == null || best.entityClass().isAssignableFrom(type))) {
                    best = defined;
                }
            }
            return Optional.ofNullable(best);
        }).orElse(null);
    }

    /** Forgets the definitions read and the entities made for type files: they're made again (after a resource reload). */
    public static void clearCache() {
        for (DefinedEntity defined : DEFAULTS) {
            defined.clearCache();
        }
        FROM_TYPES.clear();
        DEFAULT_BY_CLASS.clear();
    }

    /** An entity made from a model definition, which is read when it is first needed. */
    public static final class DefinedEntity {
        private final String modId;
        private final ResourceLocation location;
        /** Makes the entity's data from the current definition; the same factory across reloads. */
        private final IEntityDataFactory<LivingEntity> dataFactory;
        @Nullable
        private EntityModelDefinition definition;
        @Nullable
        private Class<?> entityClass;
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

        /** The entity class the definition names, or null if it can't be loaded or names none this version has. */
        @Nullable
        public Class<?> entityClass() {
            ensureLoaded();
            return entityClass;
        }

        /**
         * Its key, which its built-in type takes as its id: the mod that registered it, and the
         * definition's {@code key}, or else the 1.12 id of its entity ({@code mobends:zombie},
         * {@code mobends:zombie_pigman}), as 1.12 named it; null if the definition can't be loaded.
         */
        @Nullable
        public String key() {
            ensureLoaded();
            return key;
        }

        /**
         * Today's id of the entities its definition names ({@code minecraft:zombified_piglin}), or
         * null if it's no entity of Minecraft's (another mod's) or the definition can't be loaded.
         */
        @Nullable
        public ResourceLocation entityTypeId() {
            ensureLoaded();
            if (definition == null) {
                return null;
            }
            String legacyId = VanillaNames.legacyEntityId(definition.entity);
            return legacyId == null ? null : VanillaNames.entityType(ResourceLocation.parse(legacyId));
        }

        /** Whether it can animate {@code entity}: an instance of its entity class. */
        public boolean animates(LivingEntity entity) {
            Class<?> type = entityClass();
            return type != null && type.isInstance(entity);
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
            Class<?> type = entityClassOf(loaded.entity);
            String name = loaded.key;
            if (name == null) {
                String legacyId = VanillaNames.legacyEntityId(loaded.entity);
                if (legacyId == null) {
                    throw new MalformedKumoTemplateException("it needs a 'key': its entity " + loaded.entity + " isn't one of Minecraft's.");
                }
                ResourceLocation id = ResourceLocation.parse(legacyId);
                name = "minecraft".equals(id.getNamespace()) ? id.getPath() : id.getNamespace() + "/" + id.getPath();
            }
            definition = loaded;
            entityClass = type;
            key = modId + ":" + name;
        }

        /**
         * The class a definition's entity names: one of Minecraft's by its 1.12 name, through
         * {@link VanillaNames}; another mod's by its own name, which loaders don't rename.
         */
        private static Class<?> entityClassOf(String name) throws MalformedKumoTemplateException {
            Class<?> type = VanillaNames.entityClass(name);
            if (type == null && !name.startsWith("net.minecraft.")) {
                try {
                    // Without initialising it: a resource pack may name any class.
                    type = Class.forName(name, false, DefinedEntities.class.getClassLoader());
                } catch (ClassNotFoundException | LinkageError e) {
                    type = null;
                }
            }
            if (type == null || !LivingEntity.class.isAssignableFrom(type)) {
                throw new MalformedKumoTemplateException("its entity '" + name + "' is no living entity this version knows.");
            }
            return type;
        }

        private void clearCache() {
            definition = null;
            entityClass = null;
            key = null;
            failed = false;
            warnedModel = false;
        }
    }
}
