package net.gobbob.mobends.core.data;

import net.minecraft.entity.Entity;

public interface IEntityDataFactory<E extends Entity>
{
	EntityData<E> createEntityData(E entity);
}
