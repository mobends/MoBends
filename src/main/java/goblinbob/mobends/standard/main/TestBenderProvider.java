package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.EntityBender;
import goblinbob.mobends.core.IEntityBenderProvider;
import goblinbob.mobends.core.IRefreshable;
import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.mutation.MutationInstructions;
import goblinbob.mobends.forge.ForgeMutationContext;
import goblinbob.mobends.forge.GsonResources;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class TestBenderProvider implements IEntityBenderProvider<ForgeMutationContext>, IRefreshable
{
    private final TestDataRepository dataRepository;
    private final TestPuppeteerRepository puppeteerRepository;

    private EntityBender<ForgeMutationContext> wolfBender;

    public TestBenderProvider()
    {
        this.dataRepository = new TestDataRepository();
        this.puppeteerRepository = new TestPuppeteerRepository(dataRepository);
    }

    public void init() throws IOException
    {
        MutationInstructions instructions = GsonResources.get(new ResourceLocation(ModStatics.MODID, "mutators/wolf.json"), MutationInstructions.class);
        AnimatorTemplate animatorTemplate = GsonResources.get(new ResourceLocation(ModStatics.MODID, "animators/wolf.json"), AnimatorTemplate.class);

        this.wolfBender = new EntityBender<>(
                this.puppeteerRepository,
                "mobends:wolf",
                "wolf",
                WolfEntity.class,
                instructions,
                animatorTemplate
        );
    }

    @Override
    public void refresh()
    {
        try
        {
            this.init();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        this.puppeteerRepository.refresh();
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
