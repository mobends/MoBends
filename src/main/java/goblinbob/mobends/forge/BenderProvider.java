package goblinbob.mobends.forge;

import goblinbob.mobends.core.EntityBender;
import goblinbob.mobends.core.IEntityBenderProvider;
import goblinbob.mobends.core.exceptions.ResourceException;
import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.mutation.MutationInstructions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

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
        this.benderMap.values().forEach(b -> b.getBenderResources().clear());
    }

    @Override
    public EntityBender<ForgeMutationContext, BenderResources> getBenderForEntity(LivingEntity entity)
    {
        return this.benderMap.get(entity.getType().getRegistryName());
    }

    private <T extends LivingEntity> EntityBender<ForgeMutationContext, BenderResources> registerEntity(EntityType<?> entityType)
    {
        ResourceLocation registryName = entityType.getRegistryName();

        return this.benderMap.put(registryName, new EntityBender<>(
                this.puppeteerRepository,
                registryName.toString(),
                entityType.toString(),
                new BenderResources()
        ));
    }

    public <T extends LivingEntity> void registerMutator(EntityType<?> entityType, MutationInstructions mutationInstructions)
    {
        ResourceLocation registryName = entityType.getRegistryName();

        if (!this.benderMap.containsKey(registryName))
        {
            registerEntity(entityType);
        }

        this.benderMap.get(registryName).getBenderResources().addPartialMutator(mutationInstructions);
    }

    public <T extends LivingEntity> void registerAnimator(EntityType<?> entityType, AnimatorTemplate animatorTemplate)
    {
        ResourceLocation registryName = entityType.getRegistryName();

        if (!this.benderMap.containsKey(registryName))
        {
            registerEntity(entityType);
        }

        this.benderMap.get(registryName).getBenderResources().addPartialAnimator(animatorTemplate);
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
