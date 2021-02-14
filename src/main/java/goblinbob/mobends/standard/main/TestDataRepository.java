package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.EntityBender;
import goblinbob.mobends.core.IEntityDataRepository;
import goblinbob.mobends.forge.EntityData;
import goblinbob.mobends.forge.ForgeMutationContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.HashMap;
import java.util.Iterator;
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
