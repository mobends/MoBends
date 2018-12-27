package net.gobbob.mobends.core.pack.variable;

import java.util.HashMap;

import net.gobbob.mobends.core.EntityData;
import net.gobbob.mobends.core.LivingEntityData;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public abstract class BendsVariable
{
	public static EntityData tempData;
	public static HashMap<String, BendsVariable> variables;

	public static void init()
	{
		variables = new HashMap<String, BendsVariable>();

		registerVariable("ticks", "Ticks", new Ticks());
		registerVariable("ticksAfterPunch", "Ticks after punch", new TicksAfterPunch());
		registerVariable("health", "Health", new Health());
		registerVariable("random", "Random", new Random());
	}

	public static void registerVariable(String name, String displayName, BendsVariable variable)
	{
		variable.setDisplayName(displayName);
		variables.put(name, variable);
	}

	public static float getGlobalVar(String name)
	{
		if (tempData == null)
			return 0;
		if (variables.containsKey(name))
			return ((BendsVariable) variables.get(name)).getValue();
		return Float.POSITIVE_INFINITY;
	}

	private String displayName;

	public abstract float getValue();

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	private static class Ticks extends BendsVariable
	{
		public float getValue()
		{
			return DataUpdateHandler.getTicks();
		}
	}

	private static class TicksAfterPunch extends BendsVariable
	{
		public float getValue()
		{
			if (tempData instanceof LivingEntityData)
				return ((LivingEntityData) tempData).getTicksAfterAttack();
			else
				return 0F;
		}
	}

	private static class Health extends BendsVariable
	{
		public float getValue()
		{
			Entity entity = tempData.getEntity();
			if (entity instanceof EntityLivingBase)
				return ((EntityLivingBase) entity).getHealth();
			else
				return 0;
		}
	}

	private static class Random extends BendsVariable
	{
		public float getValue()
		{
			return (float) Math.random();
		}
	}
}
