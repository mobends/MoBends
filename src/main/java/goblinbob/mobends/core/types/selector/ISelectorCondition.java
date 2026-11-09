package goblinbob.mobends.core.types.selector;

import net.minecraft.world.entity.LivingEntity;

/**
 * A condition of a type's selector. Unlike KUMO trigger conditions, it looks at the entity itself,
 * before any entity data exists.
 */
public interface ISelectorCondition {
    boolean test(LivingEntity entity);

    /**
     * False if the answer for one entity can change during its life (the skin type does, the
     * player's name doesn't). Types whose selector isn't stable are asked again every frame.
     */
    default boolean isStable() {
        return true;
    }
}
