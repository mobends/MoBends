package net.gobbob.mobends.pack;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.util.EnumAxis;
import net.minecraft.util.MathHelper;

public class BendsAction {
	public String anim;
	public String model;
	public List<Calculation> calculations = new ArrayList<Calculation>();
	public EnumBoxProperty prop;
	public EnumAxis axis;
	public float smooth;
	public EnumModifier mod;
	public float visual_DeletePopUp;
	
	public BendsAction(String argAnim,String argModel,EnumBoxProperty argProp,EnumAxis argAxis,float argSmooth,float argNumber){
		this.anim = argAnim;
		this.model = argModel;
		this.prop = argProp;
		this.axis = argAxis;
		this.smooth = argSmooth;
		this.visual_DeletePopUp = 0;
	}
	
	public BendsAction() {
	}
	
	public BendsAction setModifier(EnumModifier argMod){
		this.mod = argMod;
		return this;
	}
	
	public float getNumber(float in){
		return Calculation.calculateAll(mod,in,calculations);
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
		SCALE,
		PREROT,
	}
	
	public enum EnumModifier{
		COS,SIN,
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
			float num = globalVar != null ? BendsVar.getGlobalVar(globalVar) : number;
			
			float out = 0;
			if(operator == EnumOperator.ADD) out = in+num;
			if(operator == EnumOperator.SET) out = num;
			if(operator == EnumOperator.SUBSTRACT) out = in-num;
			if(operator == EnumOperator.MULTIPLY) out = in*num;
			if(operator == EnumOperator.DIVIDE) out = in/num;
			return out;
		}
		
		public static float calculateAll(EnumModifier mod,float in,List<Calculation> argCalc){
			float out = in;
			for(int i = 0;i < argCalc.size();i++){
				out = argCalc.get(i).calculate(out);
			}
			if(mod == EnumModifier.COS) out = MathHelper.cos(out);
			if(mod == EnumModifier.SIN) out = MathHelper.sin(out);
			return out;
		}
	}
}
