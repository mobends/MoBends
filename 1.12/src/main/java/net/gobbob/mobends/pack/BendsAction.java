package net.gobbob.mobends.pack;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.network.NetworkConfiguration;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.gobbob.mobends.util.EnumAxis;
import net.minecraft.util.math.MathHelper;

public class BendsAction {
	public String model;
	public List<Calculation> calculations = new ArrayList<Calculation>();
	public EnumBoxProperty property;
	public EnumAxis axis;
	public float smooth;
	public EnumModifier modifier;
	
	public BendsAction(String model,EnumBoxProperty property,EnumAxis axis,float smooth){
		this.model = model;
		this.property = property;
		this.axis = axis;
		this.smooth = smooth;
	}
	
	public BendsAction() {
	}
	
	public BendsAction setModifier(EnumModifier argMod){
		this.modifier = argMod;
		return this;
	}
	
	public float getNumber(float in){
		float number = Calculation.calculateAll(modifier,in,calculations);
		if(property == EnumBoxProperty.SCALE && !NetworkConfiguration.instance.isModelScalingAllowed())
			number = Math.max(-1, Math.min(number, 1));
		return number;
	}
	
	public enum EnumOperator{
		SET,
		ADD,
		MULTIPLY,
		DIVIDE,
		SUBSTRACT,
	}
	
	public enum EnumBoxProperty{
		ROT,
		PREROT,
		SCALE
	}
	
	public enum EnumModifier{
		COS("Cosine"),SIN("Sine"),TAN("Tangent"),ABS("Absolute"),POW("Power");
		
		String displayName;
		
		EnumModifier(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}
	
	public static EnumOperator getOperatorFromSymbol(String symbol){
		return symbol.equalsIgnoreCase("+=") ? EnumOperator.ADD : symbol.equalsIgnoreCase("-=") ? EnumOperator.SUBSTRACT : symbol.equalsIgnoreCase("==") ? EnumOperator.SET : symbol.equalsIgnoreCase("*=") ? EnumOperator.MULTIPLY : EnumOperator.DIVIDE;
	}
	
	public static class Calculation{
		public EnumOperator operator;
		public float number;
		public String globalVar = null;
		
		public Calculation(EnumOperator argOperator,float argNumber){
			this.operator = argOperator;
			this.number = argNumber;
		}
		
		public Calculation setGlobalVar(String argGlobalVar){
			this.globalVar = argGlobalVar;
			return this;
		}
		
		public float calculate(float in){
			float num = globalVar != null ? BendsVariable.getGlobalVar(globalVar) : number;
			
			float out = 0;
			if(operator == EnumOperator.ADD) out = in+num;
			else if(operator == EnumOperator.SET) out = num;
			else if(operator == EnumOperator.SUBSTRACT) out = in-num;
			else if(operator == EnumOperator.MULTIPLY) out = in*num;
			else if(operator == EnumOperator.DIVIDE && num != 0) out = in/num;
			return out;
		}
		
		public static float calculateAll(EnumModifier mod,float in,List<Calculation> argCalc){
			float out = in;
			for(int i = 0;i < argCalc.size();i++){
				out = argCalc.get(i).calculate(out);
			}
			if(mod == EnumModifier.COS) out = MathHelper.cos(out);
			else if(mod == EnumModifier.SIN) out = MathHelper.sin(out);
			else if(mod == EnumModifier.TAN) out = (float) Math.tan(out);
			else if(mod == EnumModifier.ABS) out = MathHelper.abs(out);
			else if(mod == EnumModifier.POW) out = (float) Math.pow(in, out);
			return out;
		}
	}
}
