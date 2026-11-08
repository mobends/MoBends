package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.vanilla.VanillaNames;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The entities Mo' Bends animates, each made from a model definition: picks the one for an entity
 * that is drawn, by the entity class its definition names.
 */
public final class DefinedEntities {
    private static final List<DefinedEntity> ENTITIES = new ArrayList<>();

    private DefinedEntities() {
    }

    /** Animates the entities {@code <modId>:bends/models/<name>.json} defines. */
    public static void register(String modId, String name) {
        ENTITIES.add(new DefinedEntity(ModelDefinitions.locationOf(modId, name)));
    }

    /** The defined entity {@code entity} is drawn as, or null to draw it as vanilla does. */
    @Nullable
    public static DefinedEntity select(LivingEntity entity) {
        for (DefinedEntity defined : ENTITIES) {
            Class<?> entityClass = defined.entityClass();
            if (entityClass != null && entityClass.isInstance(entity)) {
                return defined;
            }
        }
        return null;
    }

    /** Forgets the definitions read, so they're read again (after a resource reload). */
    public static void clearCache() {
        for (DefinedEntity defined : ENTITIES) {
            defined.clearCache();
        }
    }

    /** An entity made from a model definition, which is read when it is first needed. */
    public static final class DefinedEntity {
        private final ResourceLocation location;
        /** Makes the entity's data from the current definition; the same factory across reloads. */
        private final IEntityDataFactory<LivingEntity> dataFactory;
        @Nullable
        private EntityModelDefinition definition;
        @Nullable
        private Class<?> entityClass;
        /** Whether the definition failed to load (logged once), until the next reload. */
        private boolean failed;

        private DefinedEntity(ResourceLocation location) {
            this.location = location;
            this.dataFactory = entity -> new DefinedEntityData<>(definition(), entity);
        }

        public ResourceLocation location() {
            return location;
        }

        public IEntityDataFactory<LivingEntity> dataFactory() {
            return dataFactory;
        }

        /** The definition; only called once {@link #entityClass()} has loaded it. */
        public EntityModelDefinition definition() {
            if (definition == null) {
                throw new IllegalStateException("The model definition " + location + " isn't loaded.");
            }
            return definition;
        }

        /** The entity class the definition names, or null if it can't be loaded or names none this version has. */
        @Nullable
        private Class<?> entityClass() {
            if (definition == null && !failed) {
                load();
            }
            return entityClass;
        }

        private void load() {
            try {
                EntityModelDefinition loaded = ModelDefinitions.INSTANCE.load(location);
                Class<?> type = VanillaNames.entityClass(loaded.entity);
                if (type == null || !LivingEntity.class.isAssignableFrom(type)) {
                    throw new IllegalArgumentException("its entity '" + loaded.entity + "' is no living entity this version knows.");
                }
                definition = loaded;
                entityClass = type;
            } catch (Exception e) {
                failed = true;
                MoBends.LOGGER.error("Mo' Bends: could not load the model definition {}: {}", location, e.getMessage(), e);
            }
        }

        private void clearCache() {
            definition = null;
            entityClass = null;
            failed = false;
        }
    }
}
