package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.*;
import goblinbob.mobends.core.exceptions.InvalidMutationException;
import goblinbob.mobends.forge.ForgeMutationContext;
import goblinbob.mobends.forge.ForgePuppeteer;
import net.minecraft.client.renderer.entity.LivingRenderer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestPuppeteerRepository implements IPuppeteerRepository<ForgeMutationContext>, IRefreshable
{
    private final IEntityDataRepository<ForgeMutationContext> dataRepository;
    private final Map<LivingRenderer<?, ?>, ForgePuppeteer<?, ?, ?>> mutatorMap = new HashMap<>();
    private final Set<LivingRenderer<?, ?>> skippedRenderers = new HashSet<>();

    public TestPuppeteerRepository(IEntityDataRepository<ForgeMutationContext> dataRepository)
    {
        this.dataRepository = dataRepository;
    }

    @Override
    public void refresh()
    {
        for (ForgePuppeteer<?, ?, ?> puppeteer : mutatorMap.values())
        {
            puppeteer.dispose();
        }

        this.mutatorMap.clear();
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

        ForgePuppeteer<?, ?, ?> mutator = mutatorMap.get(renderer);

        try
        {
            if (mutator == null)
            {
                mutator = ForgePuppeteer.create(context, bender, dataRepository);
                if (mutator != null)
                {
                    mutatorMap.put(renderer, mutator);
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
}
