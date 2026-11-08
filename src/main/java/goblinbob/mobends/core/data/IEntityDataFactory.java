package goblinbob.mobends.core.data;

import net.minecraft.world.entity.LivingEntity;

/** Makes the data of an entity; {@link EntityDatabase} initializes it. */
@FunctionalInterface
public interface IEntityDataFactory<E extends LivingEntity> {
    LivingEntityData<E> createEntityData(E entity);
}
