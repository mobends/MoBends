package goblinbob.mobends.core.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** The data of every animated entity in the client's world, by entity id. */
public class EntityDatabase {
    public static final EntityDatabase instance = new EntityDatabase();

    private final Map<Integer, LivingEntityData<?>> entryMap = new HashMap<>();

    /**
     * The factory that made each entity's data. An entity whose definition changes gets data from
     * another factory, so its data is made anew.
     */
    private final Map<Integer, IEntityDataFactory<?>> factoryMap = new HashMap<>();

    /** The entity with this id in the client's world, or null. */
    @Nullable
    private static Entity inWorld(int entityId) {
        ClientLevel level = Minecraft.getInstance().level;
        return level != null ? level.getEntity(entityId) : null;
    }

    /** The data of {@code entity}, or null if it has none (yet). */
    @Nullable
    public LivingEntityData<?> get(LivingEntity entity) {
        LivingEntityData<?> data = entryMap.get(entity.getId());
        return data != null && data.getEntity() == entity ? data : null;
    }

    /**
     * The data of {@code entity}, made by {@code factory} if it has none, or has data another
     * factory made. Null for an entity that isn't in the client's world (one a screen draws on its
     * own): its id can be another entity's, and nothing would ever drop its data.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <E extends LivingEntity> LivingEntityData<E> getOrMake(IEntityDataFactory<E> factory, E entity) {
        int entityId = entity.getId();
        if (inWorld(entityId) != entity) {
            return null;
        }
        LivingEntityData<E> data = (LivingEntityData<E>) entryMap.get(entityId);
        // Entities of another world can have had the same id.
        if (data == null || data.getEntity() != entity || factoryMap.get(entityId) != factory) {
            data = factory.createEntityData(entity);
            data.initialize();
            entryMap.put(entityId, data);
            factoryMap.put(entityId, factory);
        }
        return data;
    }

    /** At the start of every client tick: drops the data of entities that left the world, and updates the rest. */
    public void updateClient() {
        Iterator<Map.Entry<Integer, LivingEntityData<?>>> it = entryMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, LivingEntityData<?>> entry = it.next();
            LivingEntityData<?> data = entry.getValue();
            if (inWorld(entry.getKey()) != data.getEntity()) {
                factoryMap.remove(entry.getKey());
                it.remove();
            } else {
                data.updateClient();
            }
        }
    }

    /** At the start of every rendered frame. */
    public void updateRender(float partialTicks) {
        for (LivingEntityData<?> data : entryMap.values()) {
            data.update(partialTicks);
        }
    }

    /** Forgets every entity's data: after a reload, it's made anew from the new definitions and animators. */
    public void refresh() {
        entryMap.clear();
        factoryMap.clear();
    }
}
