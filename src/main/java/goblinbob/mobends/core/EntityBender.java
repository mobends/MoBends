package goblinbob.mobends.core;

import goblinbob.mobends.core.exceptions.InvalidMutationException;
import goblinbob.mobends.core.mutation.PuppeteerException;

import javax.annotation.Nonnull;

/**
 * One per mutator.
 * @param <C>
 * @param <R>
 */
public class EntityBender<C, R extends IBenderResources>
{
    private IPuppeteerRepository<C, R> puppeteerRepository;

    private String mutatorId;
    protected final String unlocalizedName;
    private final R benderResources;

    public EntityBender(IPuppeteerRepository<C, R> puppeteerRepository, String unlocalizedName, @Nonnull R benderResources)
    {
        this.puppeteerRepository = puppeteerRepository;

        this.mutatorId = benderResources.getMutationInstructions().getID();
        this.unlocalizedName = unlocalizedName;
        this.benderResources = benderResources;
    }

    public String getMutatorId()
    {
        return mutatorId;
    }

    public R getBenderResources()
    {
        return benderResources;
    }

    /**
     * This function is to be called each time before rendering a mutated entity.
     * It animates, and if it wasn't done prior, mutates the entity model.
     */
    public void beforeRender(C context, boolean animated) throws PuppeteerException, InvalidMutationException
    {
        if (animated)
        {
            IPuppeteer<C> puppeteer = puppeteerRepository.getOrCreatePuppeteer(context, this);

            if (puppeteer != null)
            {
                puppeteer.perform(context);
                puppeteer.beforeRender(context);
            }
        }
        else
        {
            puppeteerRepository.disposePuppeteer(context, this);
        }
    }
}
