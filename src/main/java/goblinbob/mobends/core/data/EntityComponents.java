package goblinbob.mobends.core.data;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * The kinds of {@link EntityComponent} a model definition can switch on, by id
 * ({@code core:orientation}, {@code mobends:sword_trail}). Addons add theirs with
 * {@code AddonAnimationRegistry.registerComponent}.
 */
public final class EntityComponents
{

    @FunctionalInterface
    public interface Factory
    {
        /** A new component for the entity whose data this is. */
        EntityComponent create(LivingEntityData<?> data);
    }

    private static final Map<String, Factory> FACTORIES = new HashMap<>();

    static
    {
        register("core:orientation", data -> new OrientationComponent());
    }

    private EntityComponents()
    {
    }

    public static void register(String kind, Factory factory)
    {
        FACTORIES.put(kind, factory);
    }

    @Nullable
    public static Factory get(String kind)
    {
        return FACTORIES.get(kind);
    }

}
