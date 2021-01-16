package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.IEntityDataRepository;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.forge.ForgeEntityData;
import goblinbob.mobends.forge.ForgeMutationContext;
import net.minecraft.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public class TestDataRepository implements IEntityDataRepository<ForgeMutationContext>
{
    private final Map<Integer, EntityData> entryMap = new HashMap<>();

    @Override
    public EntityData getOrMakeData(ForgeMutationContext context)
    {
        LivingEntity entity = context.getEntity();
        EntityData data = entryMap.get(entity.getEntityId());

        if (data == null)
        {
            data = new ForgeEntityData();
            entryMap.put(entity.getEntityId(), data);
        }

        return data;
    }
}
