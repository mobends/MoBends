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

public class EntityDatabase {
	public HashMap<Integer, EntityData> entries;
	protected final String name;
	protected final Class dataClass;
	
	public EntityDatabase(Class dataClass)
	{
		this.name = null;
		this.dataClass = dataClass;
		this.entries = new HashMap<Integer, EntityData>();
	}
	
	public EntityData getEntry(int identifier)
	{
		return entries.get(identifier);
	}
	
	public EntityData newEntry(int identifier)
	{
		EntityData data = null;
		try {
			data = (EntityData) this.dataClass.getConstructor(int.class).newInstance(identifier);
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
		if(data != null) this.entries.put(identifier, data);
		return data;
	}
	
	public void addEntry(int identifier, EntityData data)
	{
		this.entries.put(identifier, data);
	}
	
	public void removeEntry(int identifier)
	{
		this.entries.remove(identifier);
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void updateClient()
	{
		Iterator<Integer> it = entries.keySet().iterator();
		while(it.hasNext())
		{
			Integer key = it.next();
			EntityData entityData = getEntry(key);
			Entity entity = Minecraft.getMinecraft().world.getEntityByID(key);
			if(entity != null)
			{
				if(!entityData.entityType.equalsIgnoreCase(entity.getName()))
				{
					this.newEntry(key);
				}
				else
				{
					entityData.motion_prev.set(entityData.motion);
					
					entityData.motion.x = (float) entity.posX - entityData.position.x;
					entityData.motion.y = (float) entity.posY - entityData.position.y;
					entityData.motion.z = (float) entity.posZ - entityData.position.z;
			    	
					entityData.position = new Vector3f((float)entity.posX, (float)entity.posY, (float)entity.posZ);
				}
			}
			else
			{
				it.remove();
				this.removeEntry(key);
			}
		}
	}
	
	public void updateRender(float partialTicks)
	{
		for(EntityData entityData : this.entries.values())
		{
			entityData.update(partialTicks);
		}
	}
	
	public EntityData[] toArray()
	{
		if(!this.entries.isEmpty())
		{
			return (EntityData[]) this.entries.values().toArray(new EntityData[0]);
		}
		else
			return new EntityData[0];
	}
	
	public Integer[] getKeys()
	{
		return (Integer[]) this.entries.keySet().toArray(new Integer[0]);
	}
}
