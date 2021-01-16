package goblinbob.mobends.core;

public class EntityBender<C>
{
    private IMutatorRepository<C> mutatorRepository;

    protected final String key;
    protected final String unlocalizedName;
    public final Class<?> entityClass;

    private boolean animate = true;
    private boolean targetMutated = false;

    public EntityBender(IMutatorRepository<C> mutatorRepository, String key, String unlocalizedName, Class<?> entityClass)
    {
        this.mutatorRepository = mutatorRepository;

//        if (renderer == null)
//            throw new NullPointerException("The mutated renderer cannot be null.");
        if (entityClass == null)
            throw new NullPointerException("The entity class cannot be null.");

        this.key = key;
        this.unlocalizedName = unlocalizedName;
        this.entityClass = entityClass;
//        this.renderer = renderer;
    }

    public boolean isAnimated()
    {
        return animate;
    }

    public boolean applyMutation(C context)
    {
        IMutator<C> mutator = mutatorRepository.getOrCreateMutatorForContext(context);

        if (mutator == null)
        {
            return false;
        }

        if (!mutator.mutate(context))
        {
            return false;
        }

        return true;
    }
}
