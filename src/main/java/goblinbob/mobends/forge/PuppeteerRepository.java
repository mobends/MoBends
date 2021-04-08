package goblinbob.mobends.forge;

import goblinbob.mobends.core.*;
import goblinbob.mobends.core.exceptions.InvalidMutationException;
import net.minecraft.client.renderer.entity.LivingRenderer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PuppeteerRepository implements IPuppeteerRepository<ForgeMutationContext>, IRefreshable
{
    private final IEntityDataRepository<ForgeMutationContext> dataRepository;
    private final Map<LivingRenderer<?, ?>, ForgePuppeteer<?, ?, ?>> puppeteerMap = new HashMap<>();
    private final Set<LivingRenderer<?, ?>> skippedRenderers = new HashSet<>();

    public PuppeteerRepository(IEntityDataRepository<ForgeMutationContext> dataRepository)
    {
        this.dataRepository = dataRepository;
    }

    @Override
    public void refresh()
    {
        for (ForgePuppeteer<?, ?, ?> puppeteer : puppeteerMap.values())
        {
            puppeteer.dispose();
        }

        this.puppeteerMap.clear();
        this.skippedRenderers.clear();
    }

    @Override
    public IPuppeteer<ForgeMutationContext> getOrCreatePuppeteer(ForgeMutationContext context, EntityBender<ForgeMutationContext> bender) throws InvalidMutationException
    {
        LivingRenderer<?, ?> renderer = context.getRenderer();

        if (this.skippedRenderers.contains(renderer))
        {
            return null;
        }

        ForgePuppeteer<?, ?, ?> mutator = puppeteerMap.get(renderer);

        try
        {
            if (mutator == null)
            {
                mutator = ForgePuppeteer.create(context, bender, dataRepository);
                if (mutator != null)
                {
                    puppeteerMap.put(renderer, mutator);
                }
            }
        }
        catch(InvalidMutationException e)
        {
            // Skipping this renderer next time we try to create a puppeteer.
            // We treat this renderer as unfit for mutation.
            mutator = null;
            throw e;
        }
        finally
        {
            // The mutator will always be null, better to skip the checks next time.
            if (mutator == null)
            {
                skippedRenderers.add(renderer);
            }
        }

        return mutator;
    }

    @Override
    public void disposePuppeteer(ForgeMutationContext context, EntityBender<ForgeMutationContext> bender)
    {
        LivingRenderer<?, ?> renderer = context.getRenderer();

        ForgePuppeteer<?, ?, ?> puppeteer = puppeteerMap.get(renderer);

        if (puppeteer != null)
        {
            puppeteer.dispose();
            puppeteerMap.remove(renderer);
        }
    }
}
