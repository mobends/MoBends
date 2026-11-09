package goblinbob.mobends.core.client.preview;

import goblinbob.mobends.core.client.definition.DefinedEntities.DefinedEntity;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Where the previews of the Mo' Bends menu stand. 1.12 made a world of its own for them (a flat
 * floor); on 1.21.1 a client level of its own can't be made without a connection, and making one
 * tells other mods a level loaded. So a preview's entity is made in the client's level, standing
 * where the player was when it was made, but never added to it: nothing ticks it but its
 * preview, no one else sees it, and it never ends up in the world.
 * <p>
 * Each preview entity is registered here with the mob it shows, which animates it whatever type
 * would win for that entity in the game (see {@link #getSelection}). As it isn't in the level,
 * the entity database doesn't keep its data (the database keys by id, and drops what isn't in
 * the level): it's kept here until the world is {@link #close() closed}. Render thread only.
 */
public final class PreviewWorld {
    /** Every preview entity of every open world, so the renderer knows one when it draws it. */
    private static final Map<LivingEntity, Preview> PREVIEWS = new IdentityHashMap<>();

    private final List<LivingEntity> entities = new ArrayList<>();

    /** A preview entity: the mob it shows, its type (found when first drawn) and its data. */
    private static final class Preview {
        final DefinedEntity defined;
        boolean selected;
        @Nullable
        EntityTypeRegistry.Selection selection;
        @Nullable
        LivingEntityData<LivingEntity> data;
        @Nullable
        IEntityDataFactory<LivingEntity> factory;

        Preview(DefinedEntity defined) {
            this.defined = defined;
        }
    }

    /** Makes the entity a preview of {@code defined}. */
    public void register(LivingEntity entity, DefinedEntity defined) {
        PREVIEWS.put(entity, new Preview(defined));
        entities.add(entity);
    }

    /** Whether {@code entity} is a preview (of any open world): it's drawn as one, and always stands on the ground. */
    public static boolean isPreview(LivingEntity entity) {
        return PREVIEWS.containsKey(entity);
    }

    /**
     * The type the preview entity is animated by: the first one (in precedence order) that gives
     * it the mob it shows, preferring one whose selector holds for it. Null if there is none, or
     * the entity isn't a preview.
     */
    @Nullable
    public static EntityTypeRegistry.Selection getSelection(LivingEntity entity) {
        Preview preview = PREVIEWS.get(entity);
        if (preview == null) {
            return null;
        }
        if (!preview.selected) {
            preview.selected = true;
            preview.selection = EntityTypeRegistry.INSTANCE.selectionAs(preview.defined, entity);
        }
        return preview.selection;
    }

    /**
     * The preview entity's data, made by {@code factory} if it has none, or has data another
     * factory made (as {@code EntityDatabase.getOrMake} does for the entities of the level). Null
     * if the entity isn't a preview.
     */
    @Nullable
    public static LivingEntityData<LivingEntity> getOrMakeData(IEntityDataFactory<LivingEntity> factory, LivingEntity entity) {
        Preview preview = PREVIEWS.get(entity);
        if (preview == null) {
            return null;
        }
        if (preview.data == null || preview.factory != factory) {
            preview.data = factory.createEntityData(entity);
            preview.data.initialize();
            preview.factory = factory;
        }
        return preview.data;
    }

    /** The preview entity's data, or null if it has none (yet). */
    @Nullable
    static LivingEntityData<LivingEntity> dataOf(LivingEntity entity) {
        Preview preview = PREVIEWS.get(entity);
        return preview == null ? null : preview.data;
    }

    /** Drops the preview entity's data and type: both are found again when it's next drawn (the animation started over). */
    static void forget(LivingEntity entity) {
        Preview preview = PREVIEWS.get(entity);
        if (preview != null) {
            preview.selected = false;
            preview.selection = null;
            preview.data = null;
            preview.factory = null;
        }
    }

    /** Lets go of one preview entity and its data. */
    public void remove(LivingEntity entity) {
        PREVIEWS.remove(entity);
        entities.remove(entity);
    }

    /** Lets go of every preview entity of this world and their data. */
    public void close() {
        for (LivingEntity entity : entities) {
            PREVIEWS.remove(entity);
        }
        entities.clear();
    }
}
