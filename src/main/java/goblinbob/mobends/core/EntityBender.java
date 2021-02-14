package goblinbob.mobends.core;

import goblinbob.mobends.core.exceptions.InvalidMutationException;
import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.mutation.MutationInstructions;
import goblinbob.mobends.core.mutation.PuppeteerException;

public class EntityBender<C>
{
    private IPuppeteerRepository<C> puppeteerRepository;

    protected final String key;
    protected final String unlocalizedName;
    public final Class<?> entityClass;
    private MutationInstructions mutationInstructions;
    private AnimatorTemplate animatorTemplate;

    private boolean animate = true;

    public EntityBender(IPuppeteerRepository<C> puppeteerRepository, String key, String unlocalizedName, Class<?> entityClass, MutationInstructions mutationInstructions, AnimatorTemplate animatorTemplate)
    {
        this.puppeteerRepository = puppeteerRepository;

        if (entityClass == null)
            throw new NullPointerException("The entity class cannot be null.");

        this.key = key;
        this.unlocalizedName = unlocalizedName;
        this.entityClass = entityClass;
        this.mutationInstructions = mutationInstructions;
        this.animatorTemplate = animatorTemplate;
    }

    public boolean isAnimated()
    {
        return animate;
    }

    public MutationInstructions getMutationInstructions()
    {
        return mutationInstructions;
    }

    public AnimatorTemplate getAnimatorTemplate()
    {
        return animatorTemplate;
    }

    /**
     * This function is to be called every animation frame.
     * It animates, and if it wasn't done prior, mutates the entity model.
     */
    public void performPuppeteering(C context) throws PuppeteerException, InvalidMutationException
    {
        IPuppeteer<C> puppeteer = puppeteerRepository.getOrCreatePuppeteer(context, this);

        if (puppeteer != null)
        {
            puppeteer.perform(context);
        }
    }
}
