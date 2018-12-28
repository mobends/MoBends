package net.gobbob.mobends.core.mutators;

public interface IMutatorFactory<M extends Mutator>
{
	M createMutator();
}
