package net.gobbob.mobends.pack.variable;

import java.util.HashMap;

import net.gobbob.mobends.data.EntityData;

public abstract class BendsVariable {
	public static EntityData tempData;
	public static HashMap variables;
	
	public static void init() {
		variables = new HashMap<String, BendsVariable>();
		
		registerVariable("ticks", "Ticks", new Ticks());
		registerVariable("ticksAfterPunch", "Ticks after punch", new TicksAfterPunch());
		registerVariable("health", "Health", new Health());
		registerVariable("random", "Random", new Random());
	}
	
	public static void registerVariable(String name, String displayName, BendsVariable variable) {
		variable.setDisplayName(displayName);
		variables.put(name, variable);
	}
	
	public static float getGlobalVar(String name){
		if(tempData == null)
			return 0;
		if(variables.containsKey(name))
			return ((BendsVariable)variables.get(name)).getValue();
		return Float.POSITIVE_INFINITY;
	}
	
	private String displayName;
	
	public abstract float getValue();
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	private static class Ticks extends BendsVariable {
		public float getValue() {
			return tempData.getTicks();
		}
	}
	
	private static class TicksAfterPunch extends BendsVariable {
		public float getValue() {
			return tempData.getTicksAfterPunch();
		}
	}
	
	private static class Health extends BendsVariable {
		public float getValue() {
			return tempData.getEntity().getHealth();
		}
	}
	
	private static class Random extends BendsVariable {
		public float getValue() {
			return (float) Math.random();
		}
	}
}
