package net.gobbob.mobends.core.mutators;


import net.gobbob.mobends.core.data.IEntityDataFactory;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.minecraft.entity.EntityLivingBase;

@FunctionalInterface
public interface IMutatorFactory<E extends EntityLivingBase> {
	Mutator<? extends LivingEntityData<E>, ? extends E, ?> createMutator(IEntityDataFactory<E> dataFactory);
}
