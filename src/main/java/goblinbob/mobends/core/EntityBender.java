package goblinbob.mobends.core;

import goblinbob.mobends.core.exceptions.InvalidMutationException;
import goblinbob.mobends.core.mutation.PuppeteerException;

import javax.annotation.Nonnull;

public class EntityBender<C, R extends IBenderResources>
{
    private IPuppeteerRepository<C, R> puppeteerRepository;

    protected final String key;
    protected final String unlocalizedName;
    private final R benderResources;

    private boolean animate = true;

    public EntityBender(IPuppeteerRepository<C, R> puppeteerRepository, String key, String unlocalizedName, @Nonnull R benderResources)
    {
        this.puppeteerRepository = puppeteerRepository;

        this.key = key;
        this.unlocalizedName = unlocalizedName;
        this.benderResources = benderResources;
    }

    public String getKey()
    {
        return key;
    }

    public boolean isAnimated()
    {
        return animate;
    }

    public R getBenderResources()
    {
        return benderResources;
    }

    public void setAnimate(boolean animate)
    {
        this.animate = animate;
    }

    /**
     * This function is to be called each time before rendering a mutated entity.
     * It animates, and if it wasn't done prior, mutates the entity model.
     */
    public void beforeRender(C context) throws PuppeteerException, InvalidMutationException
    {
        if (this.animate)
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
