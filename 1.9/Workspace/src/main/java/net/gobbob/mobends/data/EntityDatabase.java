package net.gobbob.mobends.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class EntityDatabase {
	public HashMap entries;
	protected final String name;
	protected final Class dataClass;
	
	public EntityDatabase(Class dataClassIn) {
		this.name = null;
		this.dataClass = dataClassIn;
		this.entries = new HashMap<Integer, EntityData>();
	}
	
	public EntityData getEntry(int identifier) {
		if(entries.containsKey(identifier)) {
			return (EntityData) entries.get(identifier);
		}else{
			return null;
		}
	}
	
	public EntityData newEntry(int identifier) {
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
	
	public void addEntry(int identifier, EntityData data) {
		this.entries.put(identifier, data);
	}
	
	public void removeEntry(int identifier) {
		this.entries.remove(identifier);
	}
	
	public String getName() {
		return this.name;
	}
	
	public void updateClient() {
		Integer[] keys = this.getKeys();
		for(int i = 0; i < keys.length; i++) {
			EntityData data = ((EntityData)this.entries.get(keys[i]));
			Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(data.entityID);
			if(entity != null){
				if(!data.entityType.equalsIgnoreCase(entity.getName())){
					this.newEntry(entity.getEntityId());
				}else{
					data.motion_prev.set(data.motion);
					
					data.motion.x=(float) entity.posX-data.position.x;
					data.motion.y=(float) entity.posY-data.position.y;
					data.motion.z=(float) entity.posZ-data.position.z;
			    	
					data.position = new Vector3f((float)entity.posX,(float)entity.posY,(float)entity.posZ);
				}
			}else{
				this.removeEntry(data.entityID);
			}
		}
	}
	
	public void updateRender(float partialTicks) {
		Integer[] keys = this.getKeys();
		for(int i = 0; i < keys.length; i++) {
			((EntityData)this.entries.get(keys[i])).update(partialTicks);
		}
	}
	
	public EntityData[] toArray() {
		if(!this.entries.isEmpty()){
			return (EntityData[]) this.entries.values().toArray(new EntityData[0]);
		}else
			return new EntityData[0];
	}
	
	public Integer[] getKeys() {
		return (Integer[]) this.entries.keySet().toArray(new Integer[0]);
	}
}
