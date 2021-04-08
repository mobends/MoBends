package goblinbob.mobends.forge;

import goblinbob.mobends.core.EntityBender;
import goblinbob.mobends.core.IEntityBenderProvider;
import goblinbob.mobends.core.IRefreshable;
import goblinbob.mobends.core.kumo.ISerialContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BenderProvider implements IEntityBenderProvider<ForgeMutationContext>, IRefreshable
{
    private final ISerialContext<EntityData> serialContext;
    private final DataRepository dataRepository;
    private final PuppeteerRepository puppeteerRepository;

    private final Map<EntityType<?>, EntityBender<ForgeMutationContext>> benderMap = new HashMap<>();

    public BenderProvider(ISerialContext<EntityData> serialContext)
    {
        this.serialContext = serialContext;
        this.dataRepository = new DataRepository();
        this.puppeteerRepository = new PuppeteerRepository(dataRepository);
    }

    public ISerialContext<EntityData> getSerialContext()
    {
        return serialContext;
    }

    @Override
    public void refresh()
    {
        this.puppeteerRepository.refresh();

        for (EntityBender<ForgeMutationContext> bender : benderMap.values())
        {
            try
            {
                bender.getBenderResources().loadResources(serialContext);
            }
            catch (IOException e)
            {
                // TODO Add better error handling.
                e.printStackTrace();
            }
        }
    }

    @Override
    public EntityBender<ForgeMutationContext> getBenderForEntity(LivingEntity entity)
    {
        return this.benderMap.get(entity.getType());
    }

    public <T extends LivingEntity> EntityBender<ForgeMutationContext> registerEntity(EntityType<T> entityType, String key, String unlocalizedName, ResourceLocation instructionsPath, ResourceLocation animatorPath)
    {
        return this.benderMap.put(entityType, new EntityBender<>(
                this.puppeteerRepository,
                key,
                unlocalizedName,
                WolfEntity.class,
                new BenderResources(instructionsPath, animatorPath)
        ));
    }

    public void updateDataOnClientTick()
    {
        this.dataRepository.updateDataOnClientTick();
    }
}
