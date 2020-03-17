package goblinbob.mobends.core.data;

import net.minecraft.entity.Entity;

@FunctionalInterface
public interface IEntityDataFactory<E extends Entity>
{
	EntityData<E> createEntityData(E entity);
}
