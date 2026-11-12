package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.Ids;
import goblinbob.mobends.core.vanilla.VanillaNames;
import net.minecraft.client.model.EntityModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The entities Mo' Bends animates, each made from a model definition (1.12's benders) that a type
 * file names ({@link #forTypeFile}). Which one an entity gets is its type's choice
 * ({@code core.types.EntityTypeRegistry}).
 */
public final class DefinedEntities {
    /** Made for the type files since the last reload, in the order they were first named. */
    private static final List<DefinedEntity> FROM_TYPES = new ArrayList<>();

    private DefinedEntities() {
    }

    /** Every defined entity, made for the type files since the last reload. */
    public static List<DefinedEntity> all() {
        return new ArrayList<>(FROM_TYPES);
    }

    /**
     * The defined entity of the model definition at {@code location}, which a type file names:
     * the one already made under the same key (for another type), else a new one, which lives
     * until the next reload.
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
        for (DefinedEntity defined : FROM_TYPES) {
            if (key.equals(defined.key())) {
                return defined;
            }
        }
        return null;
    }

    /** The registry id of {@code entity}'s type ({@code minecraft:fox}). */
    public static ResourceLocation typeIdOf(LivingEntity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
    }

    /** Forgets the entities made for type files: they're made again, their definitions read anew (after a resource reload). */
    public static void clearCache() {
        FROM_TYPES.clear();
    }

    /** An entity made from a model definition, which is read when it is first needed. */
    public static final class DefinedEntity {
        private final String namespace;
        private final ResourceLocation location;
        /** Makes the entity's data from its definition. */
        private final IEntityDataFactory<LivingEntity> dataFactory;
        @Nullable
        private EntityModelDefinition definition;
        /** Today's ids of the entity types its definition names that this version has, in its order, until the next reload. */
        private List<ResourceLocation> types = List.of();
        @Nullable
        private String key;
        /** Whether the definition failed to load (logged once), until the next reload. */
        private boolean failed;
        /** Whether it was said that the definition's model has no counterpart here, until the next reload. */
        private boolean warnedModel;

        private DefinedEntity(String namespace, ResourceLocation location) {
            this.namespace = namespace;
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

        /** The definition; only called once {@link #key()} or {@link #entityTypes()} has loaded it. */
        public EntityModelDefinition definition() {
            if (definition == null) {
                throw new IllegalStateException("The model definition " + location + " isn't loaded.");
            }
            return definition;
        }

        /**
         * Its key, which the type giving it to its entities takes as its id: the namespace of its
         * definition, and the definition's {@code key}, or else one made from its first entity
         * ({@link EntityModelDefinition#defaultKey()}: {@code mobends:zombie_pigman},
         * {@code mobends:fox}); null if the definition can't be loaded.
         */
        @Nullable
        public String key() {
            ensureLoaded();
            return key;
        }

        /**
         * Today's ids of the entity types its definition names that this version has, in the order
         * it names them ({@code minecraft:zombified_piglin} for 1.12's {@code minecraft:zombie_pigman});
         * empty if the definition can't be loaded.
         */
        public List<ResourceLocation> entityTypes() {
            ensureLoaded();
            return types;
        }

        /** The first of {@link #entityTypes()}, or null if the definition can't be loaded. */
        @Nullable
        public ResourceLocation entityTypeId() {
            List<ResourceLocation> named = entityTypes();
            return named.isEmpty() ? null : named.get(0);
        }

        /** Whether it can animate {@code entity}: of an entity type it names. */
        public boolean animates(LivingEntity entity) {
            return entityTypes().contains(typeIdOf(entity));
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
            // Today's ids (1.12's renamed), those this version has (another version's mob, or a mod
            // that isn't installed, is skipped), each once.
            Set<ResourceLocation> known = new LinkedHashSet<>();
            for (String name : loaded.entity) {
                ResourceLocation id = VanillaNames.entityType(Ids.parse(name));
                if (BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                    known.add(id);
                }
            }
            if (known.isEmpty()) {
                throw new MalformedKumoTemplateException("its entity " + String.join(", ", loaded.entity) + " is no entity type this version has.");
            }
            definition = loaded;
            types = List.copyOf(known);
            key = namespace + ":" + (loaded.key != null ? loaded.key : loaded.defaultKey());
        }
    }
}
