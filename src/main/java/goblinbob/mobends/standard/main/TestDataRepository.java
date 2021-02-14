package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.EntityBender;
import goblinbob.mobends.core.IEntityDataRepository;
import goblinbob.mobends.forge.EntityData;
import goblinbob.mobends.forge.ForgeMutationContext;
import net.minecraft.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public class TestDataRepository implements IEntityDataRepository<ForgeMutationContext>
{
    private final Map<Integer, EntityData> entryMap = new HashMap<>();

    @Override
    public EntityData getOrMakeData(ForgeMutationContext context, EntityBender<ForgeMutationContext> bender)
    {
        LivingEntity entity = context.getEntity();
        EntityData data = entryMap.get(entity.getEntityId());

        if (data == null)
        {
            data = new EntityData(entity, bender.getAnimatorTemplate());
            entryMap.put(entity.getEntityId(), data);
        }

        return data;
    }
}
