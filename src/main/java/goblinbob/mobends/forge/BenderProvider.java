package goblinbob.mobends.forge;

import goblinbob.mobends.core.EntityBender;
import goblinbob.mobends.core.IEntityBenderProvider;
import goblinbob.mobends.core.exceptions.ResourceException;
import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.mutation.MutationInstructions;
import goblinbob.mobends.core.mutation.MutationMetadata;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class BenderProvider<C extends ISerialContext<C, EntityData>> implements IEntityBenderProvider<ForgeMutationContext, BenderResources>
{
    private final C serialContext;
    private final DataRepository dataRepository;
    private final PuppeteerRepository puppeteerRepository;

    private final Map<ResourceLocation, EntityBender<ForgeMutationContext, BenderResources>> benderMap = new HashMap<>();

    public BenderProvider(C serialContext)
    {
        this.serialContext = serialContext;
        this.dataRepository = new DataRepository();
        this.puppeteerRepository = new PuppeteerRepository(dataRepository);
    }

    public ISerialContext<?, EntityData> getSerialContext()
    {
        return serialContext;
    }

    public void clear()
    {
        this.puppeteerRepository.clear();
        this.benderMap.clear();
    }

    @Override
    public EntityBender<ForgeMutationContext, BenderResources> getBenderForEntity(LivingEntity entity)
    {
        return this.benderMap.get(entity.getType().getRegistryName());
    }

    public void registerBender(BenderResources benderResources)
    {
        ResourceLocation registryName = benderResources.getEntityLocation();
        EntityType<?> entityType = Registry.ENTITY_TYPE.get(registryName);

        this.benderMap.put(registryName, new EntityBender<>(
                this.puppeteerRepository,
                entityType.toString(),
                benderResources
        ));
    }

    public void finalizeEntity(EntityType<?> entityType) throws ResourceException
    {
        ResourceLocation registryName = entityType.getRegistryName();

        if (!this.benderMap.containsKey(registryName))
        {
            throw new ResourceException("Cannot finalize an entity that hasn't been registered yet.");
        }

        this.benderMap.get(registryName).getBenderResources().mergePartials();
    }

    public void updateDataOnClientTick()
    {
        this.dataRepository.updateDataOnClientTick();
    }
}
