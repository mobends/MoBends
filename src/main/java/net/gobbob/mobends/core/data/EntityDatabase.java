package net.gobbob.mobends.core.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class EntityDatabase
{
	public static EntityDatabase instance = new EntityDatabase();

	// Contains data for all EntityData instances.
	protected final HashMap<Integer, EntityData<?>> entryMap = new HashMap<>();

	public EntityData<?> get(Integer identifier)
	{
		return entryMap.get(identifier);
	}

	public EntityData<?> get(Entity entity)
	{
		return this.get(entity.getEntityId());
	}

	/**
	 * If a data instance for that identifier is null, create one. Return the data
	 * instance for that identifier.
	 * 
	 * @param dataCreationFunction The function that creates
	 * 	      and returned a new EntityData instance
	 * @param entity The entity whose data we want to get (or first create if there is none)
	 * @return Entity's data
	 */
	public <T extends EntityData<E>, E extends Entity> T getOrMake(IEntityDataFactory<E> dataCreationFunction, E entity)
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

	private void add(int identifier, EntityData<?> data)
	{
		this.entryMap.put(identifier, data);
	}

	public void add(Entity entity, EntityData<?> data)
	{
		this.add(entity.getEntityId(), data);
	}

	public void updateClient()
	{
		Iterator<Integer> it = entryMap.keySet().iterator();
		while (it.hasNext())
		{
			Integer key = it.next();
			EntityData<?> entityData = get(key);
			Entity entityInData = entityData.getEntity();
			
			if (AlterEntry.isPreviewEntity(entityInData))
			{
				entityData.updateClient();
			}
			else
			{
				Entity entity = Minecraft.getMinecraft().world.getEntityByID(key);
				if (entity != null)
				{
					if (entityData.getEntity() != entity)
					{
						it.remove();
					}
					else
					{
						entityData.updateClient();
					}
				}
				else
				{
					it.remove();
				}
			}
		}
	}

	public void updateRender(float partialTicks)
	{
		for (EntityData<?> entityData : this.entryMap.values())
		{
			if (entityData.canBeUpdated())
				entityData.update(partialTicks);
		}
	}

	public void refresh()
	{
		this.entryMap.clear();
	}

	public void onTicksRestart()
	{
		entryMap.values().forEach( data -> data.onTicksRestart() );
	}
}
