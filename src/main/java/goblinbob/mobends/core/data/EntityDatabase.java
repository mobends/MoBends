package goblinbob.mobends.core.data;

import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.bender.PreviewHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class EntityDatabase
{

    public static EntityDatabase instance = new EntityDatabase();

    /**
     * Contains data for all EntityData instances.
     */
    protected final Map<Integer, LivingEntityData<?>> entryMap = new HashMap<>();

    private LivingEntityData<?> get(Integer identifier)
    {
        return entryMap.get(identifier);
    }

    public <T extends LivingEntityData<E>, E extends EntityLivingBase> T get(E entity)
    {
        return (T) this.get(entity.getEntityId());
    }

    /**
     * If a data instance for that identifier is null, create one. Return the data
     * instance for that identifier.
     *
     * @param dataCreationFunction The function that creates
     *                             and returned a new EntityData instance
     * @param entity               The entity whose data we want to get (or first create if there is none)
     * @return Entity's data
     */
    public <T extends LivingEntityData<E>, E extends EntityLivingBase> T getOrMake(IEntityDataFactory<E> dataCreationFunction, E entity)
    {
        final int entityId = entity.getEntityId();

        // Both T and the return type of #get(Entity) will be EntityData during runtime due to generic type erasure.
        @SuppressWarnings("unchecked")
        T data = (T) this.get(entityId);

        if (data == null)
        {
            data = (T) dataCreationFunction.createEntityData(entity);
            this.add(entityId, data);
        }
        return data;
    }

    private void add(int identifier, LivingEntityData<?> data)
    {
        this.entryMap.put(identifier, data);
    }

    public void add(Entity entity, LivingEntityData<?> data)
    {
        this.add(entity.getEntityId(), data);
    }

    public void updateClient()
    {
        Iterator<Entry<Integer, LivingEntityData<?>>> it = entryMap.entrySet().iterator();
        while (it.hasNext())
        {
            Entry<Integer, LivingEntityData<?>> entry = it.next();

            LivingEntityData<?> entityData = entry.getValue();
            EntityLivingBase entityInData = entityData.getEntity();
            Entity entity = Minecraft.getMinecraft().world.getEntityByID(entry.getKey());
            if (!PreviewHelper.isPreviewEntity(entityInData) && (entity == null || entityInData != entity))
            {
                EntityBenderRegistry.instance.clearCache(entityInData);
                it.remove();
            }
            else
            {
                entityData.updateClient();
            }
        }
    }

    public void updateRender(float partialTicks)
    {
        for (EntityData<?> entityData : this.entryMap.values())
        {
            entityData.update(partialTicks);
        }
    }

    public void refresh()
    {
        this.entryMap.clear();
    }

    public void onTicksRestart()
    {
        entryMap.values().forEach(data -> data.onTicksRestart());
    }

}
