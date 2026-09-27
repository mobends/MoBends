package goblinbob.mobends.core.types.selector;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;

/**
 * A condition of a type's selector. Unlike KUMO trigger conditions, it looks at the entity itself,
 * before any entity data exists.
 */
public interface ISelectorCondition
{

    boolean test(EntityLivingBase entity);

    /**
     * False if the answer for one entity can change during its life (the skin type does, the
     * player's name doesn't). Types whose selector isn't stable are re-evaluated every frame.
     */
    default boolean isStable()
    {
        return true;
    }

    /** Adds the entity types this condition requires, so the GUI can list the type under them. */
    default void collectEntityTypes(Collection<ResourceLocation> entityTypes)
    {
    }

}
