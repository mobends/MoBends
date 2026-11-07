package goblinbob.mobends.core.data;

import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.preview.PreviewWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class EntityDatabase
{

    public static EntityDatabase instance = new EntityDatabase();

    /**
     * Contains data for all EntityData instances of entities in a world (the client's, or a preview's), by id.
     */
    protected final Map<Integer, LivingEntityData<?>> entryMap = new HashMap<>();

    /**
     * The factory that made each entity's data. An entity whose type changes gets data from another
     * factory (another model or animator), so its data is made anew.
     */
    private final Map<Integer, IEntityDataFactory<?>> factoryMap = new HashMap<>();

    /** How many client ticks the data of an entity that isn't in a world outlives its last draw. */
    private static final int DRAWN_TICKS = 20;

    /**
     * The data of entities other mods draw without adding them to a world (Just Enough Resources'
     * mob list, OpenBlocks' trophies), by the entity itself: they get their ids from the client's
     * own count, so one can share its id with an entity in the world.
     */
    private final Map<EntityLivingBase, Detached> detachedMap = new IdentityHashMap<>();

    /** Counts the client ticks, paused ones too (see {@link #tickPaused()}). */
    private int ticks;

    private static class Detached
    {
        final LivingEntityData<?> data;
        final IEntityDataFactory<?> factory;
        /** The tick the entity was last drawn in. */
        int drawnTick;

        Detached(LivingEntityData<?> data, IEntityDataFactory<?> factory, int drawnTick)
        {
            this.data = data;
            this.factory = factory;
            this.drawnTick = drawnTick;
        }
    }

    /** The entity with the id of this one in the world it is in (a preview's, or the client's), or null. */
    private static Entity inItsWorld(int entityId, EntityLivingBase entityInData)
    {
        // A preview (see PreviewWorld) is in its own world, for as long as that is open. Out of a
        // world (the menu opened from Forge's Mods list), only previews are left.
        if (entityInData.world instanceof PreviewWorld)
        {
            return entityInData.world.getEntityByID(entityId);
        }
        final World world = Minecraft.getMinecraft().world;
        return world != null ? world.getEntityByID(entityId) : null;
    }

    private static boolean isInItsWorld(EntityLivingBase entity)
    {
        return inItsWorld(entity.getEntityId(), entity) == entity;
    }

    @SuppressWarnings("unchecked")
    public <T extends LivingEntityData<E>, E extends EntityLivingBase> T get(E entity)
    {
        final Detached detached = detachedMap.get(entity);
        if (detached != null)
        {
            return (T) detached.data;
        }
        return (T) entryMap.get(entity.getEntityId());
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
    @SuppressWarnings("unchecked")
    public <T extends LivingEntityData<E>, E extends EntityLivingBase> T getOrMake(IEntityDataFactory<E> dataCreationFunction, E entity)
    {
        if (!isInItsWorld(entity))
        {
            // Drawn without being in a world: its data lasts while it is drawn, or it would start
            // from its first pose every tick.
            Detached detached = detachedMap.get(entity);
            if (detached == null || detached.factory != dataCreationFunction)
            {
                if (detached != null)
                {
                    EntityBenderRegistry.instance.clearCache(entity);
                }
                detached = new Detached(make(dataCreationFunction, entity), dataCreationFunction, ticks);
                detachedMap.put(entity, detached);
            }
            detached.drawnTick = ticks;
            return (T) detached.data;
        }

        final int entityId = entity.getEntityId();
        T data = (T) entryMap.get(entityId);

        // Entities of another world can have had the same id.
        if (data == null || data.getEntity() != entity || factoryMap.get(entityId) != dataCreationFunction)
        {
            data = make(dataCreationFunction, entity);
            this.entryMap.put(entityId, data);
            this.factoryMap.put(entityId, dataCreationFunction);
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    private static <T extends LivingEntityData<E>, E extends EntityLivingBase> T make(IEntityDataFactory<E> dataCreationFunction, E entity)
    {
        final T data = (T) dataCreationFunction.createEntityData(entity);
        data.initialize();
        return data;
    }

    public void add(Entity entity, LivingEntityData<?> data)
    {
        this.entryMap.put(entity.getEntityId(), data);
    }

    public void updateClient()
    {
        countTick();

        Iterator<Entry<Integer, LivingEntityData<?>>> it = entryMap.entrySet().iterator();
        while (it.hasNext())
        {
            Entry<Integer, LivingEntityData<?>> entry = it.next();

            LivingEntityData<?> entityData = entry.getValue();
            EntityLivingBase entityInData = entityData.getEntity();
            if (inItsWorld(entry.getKey(), entityInData) != entityInData)
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

        for (Detached detached : detachedMap.values())
        {
            detached.data.updateClient();
        }
    }

    /**
     * A client tick while the game is paused, when the entities don't update: the window in which
     * an entity outside a world keeps its data still counts it, as the GUIs other mods draw such
     * entities in can pause the game.
     */
    public void tickPaused()
    {
        countTick();
    }

    /** Counts a client tick, and drops the data of entities outside a world that weren't drawn in the last {@link #DRAWN_TICKS}. */
    private void countTick()
    {
        ++ticks;
        Iterator<Entry<EntityLivingBase, Detached>> it = detachedMap.entrySet().iterator();
        while (it.hasNext())
        {
            Entry<EntityLivingBase, Detached> entry = it.next();
            if (ticks - entry.getValue().drawnTick > DRAWN_TICKS)
            {
                EntityBenderRegistry.instance.clearCache(entry.getKey());
                it.remove();
            }
        }
    }

    public void updateRender(float partialTicks)
    {
        for (EntityData<?> entityData : this.entryMap.values())
        {
            entityData.update(partialTicks);
        }
        for (Detached detached : detachedMap.values())
        {
            detached.data.update(partialTicks);
        }
    }

    public void refresh()
    {
        this.entryMap.clear();
        this.factoryMap.clear();
        this.detachedMap.clear();
    }

}
