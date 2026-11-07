package goblinbob.mobends.core.data;

import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.preview.PreviewWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

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

    /**
     * The factory that made each entity's data. An entity whose type changes gets data from another
     * factory (another model or animator), so its data is made anew.
     */
    private final Map<Integer, IEntityDataFactory<?>> factoryMap = new HashMap<>();

    /** How many ticks the data of an entity that isn't in the world outlives its last draw. */
    private static final int DRAWN_TICKS = 20;
    /** The frame each of the last {@link #DRAWN_TICKS} client ticks began in, a ring from {@link #tickIndex}. */
    private final int[] tickFrames = new int[DRAWN_TICKS];
    private int tickIndex;

    private LivingEntityData<?> get(Integer identifier)
    {
        return entryMap.get(identifier);
    }

    public <T extends LivingEntityData<E>, E extends EntityLivingBase> T get(E entity)
    {
        return (T) this.get(entity.getEntityId());
    }

    /**
     * If there is no data instance for that entity, or it was made by another factory, create one.
     * Return the data instance for that entity.
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

        // Entities of another world can have had the same id.
        if (data == null || data.getEntity() != entity || factoryMap.get(entityId) != dataCreationFunction)
        {
            data = (T) dataCreationFunction.createEntityData(entity);
            data.initialize();
            this.add(entityId, data);
            this.factoryMap.put(entityId, dataCreationFunction);
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
        // The frame this tick began in, DRAWN_TICKS ticks ago.
        final int drawnSince = tickFrames[tickIndex];
        tickFrames[tickIndex] = DataUpdateHandler.frame;
        tickIndex = (tickIndex + 1) % DRAWN_TICKS;

        Iterator<Entry<Integer, LivingEntityData<?>>> it = entryMap.entrySet().iterator();
        while (it.hasNext())
        {
            Entry<Integer, LivingEntityData<?>> entry = it.next();

            LivingEntityData<?> entityData = entry.getValue();
            EntityLivingBase entityInData = entityData.getEntity();
            // A preview (see PreviewWorld) is in its own world, for as long as that is open. Out
            // of a world (the menu opened from Forge's Mods list), only previews are left.
            final World world = Minecraft.getMinecraft().world;
            Entity entity = entityInData.world instanceof PreviewWorld
                    ? entityInData.world.getEntityByID(entry.getKey())
                    : world != null ? world.getEntityByID(entry.getKey()) : null;
            // An entity that isn't in any world we know, but is drawn (another mod's: Just Enough
            // Resources' mob list, OpenBlocks' trophies), keeps its data while it is, or it would
            // start from its first pose every tick.
            boolean drawnOutsideTheWorld = entity == null && entityData.getAnimatedFrame() >= drawnSince;
            if ((entity == null || entityInData != entity) && !drawnOutsideTheWorld)
            {
                EntityBenderRegistry.instance.clearCache(entityInData);
                factoryMap.remove(entry.getKey());
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
        this.factoryMap.clear();
    }

}
