package net.gobbob.mobends.util;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;

/*
 * Used to more efficiently mine for protected fields
 */
public class FieldMiner
{
	public static Field getField(Class classIn, String fieldName) throws NoSuchFieldException
	{
		try
		{
			return classIn.getDeclaredField(fieldName);
		}
		catch (NoSuchFieldException e)
		{
			Class superClass = classIn.getSuperclass();
			if (superClass == null)
			{
				throw e;
			}
			else
			{
				return getField(superClass, fieldName);
			}
		}
	}
	
	public static Field getObfuscatedField(Class classIn, String fieldName, String fieldNameObfuscated)
	{
		Field field = null;
		
		try
		{
			field = getField(classIn, fieldNameObfuscated);
		}
		catch (NoSuchFieldException e)
		{
			try
			{
				field = getField(classIn, fieldName);
			}
			catch (NoSuchFieldException e1) {}
		}
		catch (SecurityException | IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		
		return field;
	}
	
	public static <T> T getObfuscatedValue(Object object, String fieldName, String fieldNameObfuscated)
	{
		Field field = FieldMiner.getObfuscatedField(object.getClass(), fieldName, fieldNameObfuscated);
		if (field != null)
		{
			field.setAccessible(true);
			T value = null;
			try
			{
				value = (T) field.get(object);
			}
			catch (IllegalArgumentException | IllegalAccessException e) {}
			return value;
		}
		return null;
	}

	public static void printFields(Class classIn)
	{
		Field[] fields = classIn.getDeclaredFields();
		for(Field f : fields)
		{
			System.out.println(" --Field: " + f);
		}
	}
}
