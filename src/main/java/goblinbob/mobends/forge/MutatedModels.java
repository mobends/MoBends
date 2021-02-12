package goblinbob.mobends.forge;

import net.minecraft.client.renderer.entity.model.EntityModel;

import java.util.HashSet;
import java.util.Set;

public class MutatedModels
{
    private static final Set<EntityModel<?>> mutatedModels = new HashSet<>();

    public static void markMutated(EntityModel<?> model)
    {
        mutatedModels.add(model);
    }

    public static boolean isMutated(EntityModel<?> model)
    {
        return mutatedModels.contains(model);
    }

    public static void markVanilla(EntityModel<?> model)
    {
        mutatedModels.remove(model);
    }
}
