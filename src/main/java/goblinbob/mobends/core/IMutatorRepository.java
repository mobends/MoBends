package goblinbob.mobends.core;

public interface IMutatorRepository<C>
{
    IMutator<C> getOrCreateMutatorForContext(C context);
}
