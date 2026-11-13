package goblinbob.mobends.core.compat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * What the shared assets ({@code assets/mobends/bends/}, copied from {@code main} as they are, see
 * CONTRIBUTING.md) name that 1.8.9 has under another name, or doesn't have at all. The assets are
 * never edited to fit 1.8.9: they're read through this instead.
 */
public final class SharedAssets
{

    /** Entity classes of later versions that are a variant of another class in 1.8.9 ({@link EntityIds} tells them apart). */
    private static final Map<String, String> ENTITY_CLASSES = new HashMap<>();

    static
    {
        ENTITY_CLASSES.put("net.minecraft.entity.monster.EntityZombieVillager", "net.minecraft.entity.monster.EntityZombie");
        ENTITY_CLASSES.put("net.minecraft.entity.monster.EntityWitherSkeleton", "net.minecraft.entity.monster.EntitySkeleton");
    }

    /**
     * Entity classes of later versions that 1.8.9 doesn't have at all. No type file of the mod's names
     * their models, so nothing loads them here; they're shipped as they are on {@code main}.
     */
    private static final Set<String> MISSING_ENTITY_CLASSES = Collections.singleton("net.minecraft.entity.monster.EntityPolarBear");

    /** Layers a model definition can name that 1.8.9 has nothing to draw with (there's no elytra). */
    private static final Set<String> MISSING_LAYERS = Collections.singleton("mobends:elytra");

    private SharedAssets()
    {
    }

    /** The 1.8.9 class of the entity class a model definition names. */
    public static String entityClass(String name)
    {
        String mapped = ENTITY_CLASSES.get(name);
        return mapped != null ? mapped : name;
    }

    /** Whether 1.8.9 has no such entity class, and no variant of another class that stands for it. */
    public static boolean isMissingEntityClass(String name)
    {
        return MISSING_ENTITY_CLASSES.contains(name);
    }

    /** Whether the layer is one 1.8.9 doesn't have, so a definition naming it leaves it out without a warning. */
    public static boolean isMissingLayer(String id)
    {
        return MISSING_LAYERS.contains(id);
    }

}
