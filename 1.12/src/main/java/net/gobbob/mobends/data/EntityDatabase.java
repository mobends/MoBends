package net.gobbob.mobends.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;

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
	
	public EntityData newEntry(Class dataClass, Entity entity)
	{
		EntityData data = null;
		try {
			data = (EntityData) dataClass.getConstructor(Entity.class).newInstance(entity);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		if(data != null) this.add(entity.getEntityId(), data);
		return data;
	}
	
	/*
	 * If a data instance for that identifier is null, create one.
	 * Return the data instance for that identifier.
	 * */
	public EntityData getAndMake(Class dataClass, Entity entity) {
		EntityData data = this.get(entity.getEntityId());
		if(data == null)
			data = this.newEntry(dataClass, entity);
		return data;
	}
	
	public void add(int identifier, EntityData data)
	{
		this.entryMap.put(identifier, data);
	}
	
	public void add(Entity entity, EntityData data)
	{
		this.add(entity.getEntityId(), data);
	}
	
	public void remove(int identifier)
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
			entityData.update(partialTicks);
		}
	}
}
