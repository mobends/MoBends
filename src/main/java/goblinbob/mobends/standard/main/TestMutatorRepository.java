package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.IEntityDataRepository;
import goblinbob.mobends.core.IMutator;
import goblinbob.mobends.core.IMutatorRepository;
import goblinbob.mobends.forge.ForgeMutationContext;
import goblinbob.mobends.forge.ForgeMutator;
import net.minecraft.client.renderer.entity.LivingRenderer;

import java.util.HashMap;
import java.util.Map;

public class TestMutatorRepository implements IMutatorRepository<ForgeMutationContext>
{

    private final IEntityDataRepository<ForgeMutationContext> dataRepository;
    private final Map<LivingRenderer<?, ?>, ForgeMutator<?, ?, ?>> mutatorMap = new HashMap<>();

    public TestMutatorRepository(IEntityDataRepository<ForgeMutationContext> dataRepository)
    {
        this.dataRepository = dataRepository;
    }

    @Override
    public IMutator<ForgeMutationContext> getOrCreateMutatorForContext(ForgeMutationContext context)
    {
        LivingRenderer<?, ?> renderer = context.getRenderer();
        ForgeMutator<?, ?, ?> mutator = mutatorMap.get(renderer);

        if (mutator == null)
        {
            //noinspection rawtypes
            mutator = new ForgeMutator(dataRepository);
            mutatorMap.put(renderer, mutator);
        }

        return mutator;
    }
}
