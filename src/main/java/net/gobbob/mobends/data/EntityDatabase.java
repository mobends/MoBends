package net.gobbob.mobends.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class EntityDatabase {
	public static EntityDatabase instance = new EntityDatabase();
	
	// Contains data for all EntityData instances.
	protected HashMap<Integer, EntityData> entryMap;
	
	public EntityDatabase()
	{
		this.entryMap = new HashMap<Integer, EntityData>();
	}
	
	public EntityData get(Integer identifier)
	{
		return entryMap.get(identifier);
	}
	
	public EntityData get(Entity entity)
	{
		return this.get(entity.getEntityId());
	}
	
	/*
	 * If a data instance for that identifier is null, create one.
	 * Return the data instance for that identifier.
	 * */
	public EntityData getAndMake(Function<Entity, EntityData> dataCreationFunction, Entity entity) {
		EntityData data = this.get(entity.getEntityId());
		if (data == null) {
			data = dataCreationFunction.apply(entity);
			if (data != null)
			{
				this.add(entity.getEntityId(), data);
			}
		}
		return data;
	}
	
	private void add(int identifier, EntityData data)
	{
		this.entryMap.put(identifier, data);
	}
	
	public void add(Entity entity, EntityData data)
	{
		this.add(entity.getEntityId(), data);
	}
	
	private void remove(int identifier)
	{
		this.entryMap.remove(identifier);
	}
	
	public void updateClient()
	{
		Iterator<Integer> it = entryMap.keySet().iterator();
		while(it.hasNext())
		{
			Integer key = it.next();
			EntityData entityData = get(key);
			Entity entity = Minecraft.getMinecraft().world.getEntityByID(key);
			if(entity != null)
			{
				if(entityData.getEntity() != entity)
				{
					System.out.println("Removing");
					it.remove();
				}
				else
				{
					entityData.updateClient(entity);
				}
			}
			else
			{
				it.remove();
				this.remove(key);
			}
		}
	}
	
	public void updateRender(float partialTicks)
	{
		for(EntityData entityData : this.entryMap.values())
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
