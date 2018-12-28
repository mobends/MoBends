package net.gobbob.mobends.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;

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

	/*
	 * If a data instance for that identifier is null, create one. 
	 * 
	 * 
	 * @return The data instance for the specified identifier.
	 */
	public <T extends EntityData<E>, E extends Entity> T getAndMake(Function<E, T> dataCreationFunction, E entity)
	{
		final int entityId = entity.getEntityId();
		
		// Both T and the return type of #get(Entity) will be EntityData during runtime due to generic type erasure. 
		@SuppressWarnings("unchecked")
		T data = (T) this.get(entityId);
		
		if (data == null)
		{
			data = dataCreationFunction.apply(entity);
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
			Entity entity = Minecraft.getMinecraft().world.getEntityByID(key);
			if (entity != null)
			{
				if (entityData.getEntity() != entity)
				{
					it.remove();
				} else
				{
					entityData.updateClient(entity);
				}
			} else
			{
				it.remove();
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
}
