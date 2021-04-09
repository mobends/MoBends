package goblinbob.mobends.forge;

import goblinbob.mobends.core.EntityBender;
import goblinbob.mobends.core.IEntityDataRepository;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DataRepository implements IEntityDataRepository<ForgeMutationContext, BenderResources>
{
    private final Map<Integer, EntityData> entryMap = new HashMap<>();

    @Override
    public EntityData getOrMakeData(ForgeMutationContext context, EntityBender<ForgeMutationContext, BenderResources> bender)
    {
        LivingEntity entity = context.getEntity();
        EntityData data = entryMap.get(entity.getId());

        if (data == null)
        {
            data = new EntityData(entity, bender.getBenderResources().getAnimatorTemplate(), context);
            entryMap.put(entity.getId(), data);
        }

        return data;
    }

    public void updateDataOnClientTick()
    {
        Iterator<Map.Entry<Integer, EntityData>> it = entryMap.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<Integer, EntityData> entry = it.next();
            EntityData entityData = entry.getValue();
            Entity entity = entityData.getEntity();

            if (!entity.isAlive())
            {
                it.remove();
            }
            else
            {
                entityData.updateClient();
            }
        }
    }
}
