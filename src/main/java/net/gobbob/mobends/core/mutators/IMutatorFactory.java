package net.gobbob.mobends.core.mutators;

import net.gobbob.mobends.core.data.IEntityDataFactory;

public interface IMutatorFactory<M extends Mutator>
{
	M createMutator(IEntityDataFactory dataFactory);
}
