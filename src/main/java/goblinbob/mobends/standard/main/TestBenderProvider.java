package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.EntityBender;
import goblinbob.mobends.core.IEntityBenderProvider;
import goblinbob.mobends.forge.ForgeMutationContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.WolfEntity;

public class TestBenderProvider implements IEntityBenderProvider<ForgeMutationContext>
{

    private final TestMutatorRepository mutatorRepository;
    private final TestDataRepository dataRepository;

    private final EntityBender<ForgeMutationContext> wolfBender;

    public TestBenderProvider()
    {
        this.dataRepository = new TestDataRepository();
        this.mutatorRepository = new TestMutatorRepository(dataRepository);
        this.wolfBender = new EntityBender<>(this.mutatorRepository, "mobends:wolf", "wolf", WolfEntity.class);
    }

    @Override
    public EntityBender<ForgeMutationContext> getBenderForEntity(LivingEntity entity)
    {
        if (entity instanceof WolfEntity)
        {
            return wolfBender;
        }

        return null;
    }

}
