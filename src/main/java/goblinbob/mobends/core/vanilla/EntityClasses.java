package goblinbob.mobends.core.vanilla;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The class of the entities of an entity type, which the registry doesn't say (1.12's
 * {@code EntityList.getClass}): found by making one, in the client's level but never added to it,
 * the first time it's asked. Render thread only.
 */
public final class EntityClasses {
    private static final ResourceLocation PLAYER = ResourceLocation.withDefaultNamespace("player");
    private static final Map<ResourceLocation, Optional<Class<? extends Entity>>> CLASSES = new HashMap<>();

    private EntityClasses() {
    }

    /**
     * The class of the entities of the type {@code id}, or null if there's no such type, or none can
     * be made (also while there's no level to make one in; that isn't remembered).
     */
    @Nullable
    public static Class<? extends Entity> of(ResourceLocation id) {
        if (PLAYER.equals(id)) {
            return AbstractClientPlayer.class;
        }
        Optional<Class<? extends Entity>> known = CLASSES.get(id);
        if (known != null) {
            return known.orElse(null);
        }
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        Class<? extends Entity> found = null;
        Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
        if (type.isPresent()) {
            try {
                Entity made = type.get().create(level);
                found = made == null ? null : made.getClass();
            } catch (RuntimeException | LinkageError e) {
                found = null;
            }
        }
        CLASSES.put(id, Optional.ofNullable(found));
        return found;
    }
}
