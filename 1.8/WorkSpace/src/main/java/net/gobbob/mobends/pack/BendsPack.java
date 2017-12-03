package net.gobbob.mobends.pack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.MoBends;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.client.model.entity.ModelBendsSpider;
import net.gobbob.mobends.client.model.entity.ModelBendsZombie;
import net.gobbob.mobends.pack.BendsAction.Calculation;
import net.gobbob.mobends.pack.BendsAction.EnumBoxProperty;
import net.gobbob.mobends.pack.BendsAction.EnumModifier;
import net.gobbob.mobends.pack.BendsAction.EnumOperator;
import net.gobbob.mobends.util.BendsLogger;
import net.gobbob.mobends.util.EnumAxis;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;

public class BendsPack {
	public static File bendsPacksDir;
	
	public static List<BendsPack> bendsPacks = new ArrayList<BendsPack>();
	
	public static int currentPack = 0;
	
	public String filename;
	public String displayName;
	public String author;
	public String description;
	public static List<BendsTarget> targets = new ArrayList<BendsTarget>();
	
	public BendsPack(){
		
	}
	
	public void readBasicInfo(File file) throws IOException{
		this.filename = file.getName();
		
		BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
		
		String line = reader.readLine();
		while(line != null){
			BendsLogger.log(line, BendsLogger.DEBUG);
			
			if(line.startsWith("name:")){
				String data = line;
				this.displayName = formatStringData("name:", data);
			}else if(line.startsWith("author:")){
				String data = line;
				this.author = formatStringData("author:", data);
			}else if(line.startsWith("description:")){
				String data = line;
				this.description = formatStringData("description:", data);
			}
			
			line = reader.readLine();
		}
		
		reader.close();
	}
	
	public void apply() throws IOException{
		if(filename == null) {
			targets.clear();
			return;
		}
		File file = new File(bendsPacksDir,filename);
		BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
		targets.clear();
		boolean override = false;
		String anim = "";
		
		String line = reader.readLine();
		while(line != null){
			if(line.startsWith("target")){
				String data = line;
				data = formatStringData("target", data);
				targets.add(new BendsTarget(data.toLowerCase()));
				override = false;
			}else if(line.contains("anim")){
				String data = line;
				anim = formatStringData("anim", data);
			}else if(line.contains("override: true")){
				override = true;
			}else if(line.contains("override: false")){
				override = false;
			}else if(line.contains("@")){
				if(targets.size() > 0){
					targets.get(targets.size()-1).actions.add(getActionFromLine(anim,line));
				}
			}
			
			line = reader.readLine();
		}
		
		reader.close();
		
		for(int i = 0;i < targets.size();i++){
			System.out.println("Target: " + targets.get(i).mob);
			for(int a = 0;a < targets.get(i).actions.size();a++){
				System.out.println("    Action: " + targets.get(i).actions.get(a).anim + ", "  + targets.get(i).actions.get(a).model + ", " + targets.get(i).actions.get(a).prop.name() + "-" + (targets.get(i).actions.get(a).axis != null ? targets.get(i).actions.get(a).axis.name() : "null") + (targets.get(i).actions.get(a).mod != null ? targets.get(i).actions.get(a).mod.name() : "null"));
				for(int c = 0;c < targets.get(i).actions.get(a).calculations.size();c++){
					Calculation calc = targets.get(i).actions.get(a).calculations.get(c);
					System.out.println("        Calc: " + calc.operator.name() + ", " + (calc.globalVar != null ? calc.globalVar : calc.number) + ", ");
				}
			}
		}
	}
	
	public static void preInit(Configuration config){
		bendsPacksDir = new File(Minecraft.getMinecraft().mcDataDir,"bendspacks");
    	bendsPacksDir.mkdir();
    	
        BendsPack.currentPack = config.get("General", "Current Pack", 0).getInt();
        
        try {
			initPacks(); 
	        if(getCurrentPack() != null){
	        	getCurrentPack().apply();
	        }
        } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void initPacks() throws IOException{
		File[] files = bendsPacksDir.listFiles();
		
		bendsPacks.clear();
		
		bendsPacks.add(getDefaultPack());
		
		for(File file : files){
			if(file.getAbsolutePath().endsWith(".bends")){
				BendsLogger.log(file.getAbsolutePath(),BendsLogger.DEBUG);
				BendsPack pack = new BendsPack();
				pack.readBasicInfo(file);
				if(pack.filename != null & pack.displayName != null)
				bendsPacks.add(pack);
			}
		}
		
		if(currentPack > bendsPacks.size()-1){
			currentPack = bendsPacks.size()-1;
		}
	}
	
	public static BendsPack getDefaultPack(){
		BendsPack defaultPack = new BendsPack();
		defaultPack.filename = null;
		defaultPack.displayName = "Default";
		defaultPack.author = "GoblinBob";
		defaultPack.description = "The default bends-pack suggested and made by GoblinBob, the creator of Mo' Bends.";
		return defaultPack;
	}
	
	public static String formatStringData(String header,String data){
		data = data.replaceFirst(header, "");
		if(data.contains("\"")) data = data.replaceAll("\"", "");
		if(data.contains("{")) data = data.replace("{", "");
		data = data.trim();
		return data;
	}
	
	public static BendsPack getCurrentPack(){
		if(currentPack > bendsPacks.size()-1){
			currentPack = bendsPacks.size()-1;
		}
		return bendsPacks.get(currentPack);
	}
	
	public static BendsAction getActionFromLine(String anim,String line){
		BendsAction action = new BendsAction();
		action.anim = anim;
		action.model = "";
		class Operation{
			public String operator = "";
			public String num = "";
			public String globalVar = null;
		}
		
		List<Operation> calcs = new ArrayList<Operation>();
		calcs.add(new Operation());
		int calc = 0;
		String smooth = "";
		
		int stage = 0;
		for(int i = 0;i < line.length();i++){
			if(stage == 0){
				if(line.charAt(i) == '@')
					stage = 1;
			}else{
				if(stage == 1){
					if(line.charAt(i) == ':'){
						stage++;
					}else{
						action.model+=line.charAt(i);
					}
				}else if(stage == 2){
					if(line.charAt(i) == ' ')
						stage++;
				}else if(stage == 3){
					if(line.charAt(i) == ' ')
						stage++;
					else
						calcs.get(calc).operator+=line.charAt(i);
				}else if(stage == 4){
					if(line.charAt(i) == ' '){
						stage++;
					}else{
						if(line.charAt(i) == '+' |
						   line.charAt(i) == '-' |
						   line.charAt(i) == '=' |
						   line.charAt(i) == '*' |
						   line.charAt(i) == '/'){
							if(line.charAt(i+1) == '='){
								calcs.add(new Operation());
								calc++;
								calcs.get(calc).operator =  line.charAt(i)+"=";
								i++;
							}else{
								calcs.get(calc).num+=line.charAt(i);
							}
						}else{
							calcs.get(calc).num+=line.charAt(i);
						}
					}
				}else if(stage == 5){
					if(line.charAt(i) == ' ')
						stage++;
					else
						smooth+=line.charAt(i) == '#' ? "" : line.charAt(i);
				}
			}
		}
		
		anim = anim.trim();
		for(int i = 0;i < calcs.size();i++){
			calcs.get(i).num = calcs.get(i).num.trim();
			
			if(calcs.get(i).num.contains(":cos:")){
				action.mod = EnumModifier.COS;
				calcs.get(i).num = calcs.get(i).num.replaceAll(":cos:","");
				calcs.get(i).num = calcs.get(i).num.trim();
			}
			
			if(calcs.get(i).num.contains(":sin:")){
				action.mod = EnumModifier.SIN;
				calcs.get(i).num = calcs.get(i).num.replaceAll(":sin:","");
				calcs.get(i).num = calcs.get(i).num.trim();
			}
			
			if(calcs.get(i).num.contains("$")){
				calcs.get(i).num = calcs.get(i).num.replace("$", " ");
				calcs.get(i).num = calcs.get(i).num.trim();
				calcs.get(i).globalVar = calcs.get(i).num;
				calcs.get(i).num = "0";
				
				System.out.println("Global Var Used: " + calcs.get(i).globalVar);
			}
			calcs.get(i).operator = calcs.get(i).operator.trim();
			
			System.out.println("Number: " + calcs.get(i).num + ", " + calcs.get(i).operator + ";");
			System.out.println("Line: " + line);
			
			action.calculations.add(new Calculation(BendsAction.getOperatorFromSymbol(calcs.get(i).operator),Float.parseFloat(calcs.get(i).num)).setGlobalVar(calcs.get(i).globalVar));
		}
		
		if(line.contains(":rot:"))
			action.prop = EnumBoxProperty.ROT;
		else if(line.contains(":scale:"))
			action.prop = EnumBoxProperty.SCALE;
		else if(line.contains(":prerot:"))
			action.prop = EnumBoxProperty.PREROT;
		
		if(line.contains(":x"))
			action.axis = EnumAxis.X;
		else if(line.contains(":y"))
			action.axis = EnumAxis.Y;
		else if(line.contains(":z"))
			action.axis = EnumAxis.Z;
		
		action.smooth = Float.parseFloat(smooth);
		
		return action;
	}
	
	public static BendsTarget getTargetByID(String argID){
		for(int i = 0;i < targets.size();i++){
			if(targets.get(i).mob.equalsIgnoreCase(argID)){
				return targets.get(i);
			}
		}
		return null;
	}
	
	public static void animate(ModelBendsPlayer model,String target,String anim){
		if(getTargetByID(target) == null)
			return;
		
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedBody, anim, "body");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedHead, anim, "head");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedLeftArm, anim, "leftArm");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedRightArm, anim, "rightArm");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedLeftLeg, anim, "leftLeg");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedRightLeg, anim, "rightLeg");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedLeftForeArm, anim, "leftForeArm");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedRightForeArm, anim, "rightForeArm");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedLeftForeLeg, anim, "leftForeLeg");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedRightForeLeg, anim, "rightForeLeg");
		
		getTargetByID(target).applyToModel(model.renderItemRotation, anim, "itemRotation");
		getTargetByID(target).applyToModel(model.renderRotation, anim, "playerRotation");
	}
	
	public static void animate(ModelBendsZombie model,String target,String anim){
		if(getTargetByID(target) == null)
			return;
		
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedBody, anim, "body");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedHead, anim, "head");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedLeftArm, anim, "leftArm");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedRightArm, anim, "rightArm");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedLeftLeg, anim, "leftLeg");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedRightLeg, anim, "rightLeg");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedLeftForeArm, anim, "leftForeArm");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedRightForeArm, anim, "rightForeArm");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedLeftForeLeg, anim, "leftForeLeg");
		getTargetByID(target).applyToModel((ModelRendererBends) model.bipedRightForeLeg, anim, "rightForeLeg");
	}
	
	public static void animate(ModelBendsSpider model,String target,String anim){
		if(getTargetByID(target) == null)
			return;
		
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderBody, anim, "body");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderNeck, anim, "neck");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderHead, anim, "head");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderLeg1, anim, "leg1");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderLeg2, anim, "leg2");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderLeg3, anim, "leg3");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderLeg4, anim, "leg4");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderLeg5, anim, "leg5");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderLeg6, anim, "leg6");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderLeg7, anim, "leg7");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderLeg8, anim, "leg8");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderForeLeg1, anim, "foreLeg1");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderForeLeg2, anim, "foreLeg2");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderForeLeg3, anim, "foreLeg3");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderForeLeg4, anim, "foreLeg4");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderForeLeg5, anim, "foreLeg5");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderForeLeg6, anim, "foreLeg7");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderForeLeg7, anim, "foreLeg7");
		getTargetByID(target).applyToModel((ModelRendererBends) model.spiderForeLeg8, anim, "foreLeg8");
	}
	
	public void save() throws IOException{
			String tab = "	";
		
			BendsLogger.log("Saving Pack "+this.displayName+"...", BendsLogger.DEBUG);
			
			if(this.filename == null){
				this.filename = constructFilenameWithDisplayName(this.displayName);
			}
			
			for(int s = 0;s < this.targets.size();s++){
				BendsLogger.log("    -"+this.targets.get(s).actions.size(), BendsLogger.DEBUG);
			}
			
			File packFile = new File(bendsPacksDir,this.filename+"");
			packFile.createNewFile();
			
			BufferedWriter os = new BufferedWriter(new FileWriter(packFile));
			
			os.write("name: \""+this.displayName+"\"\n");
			os.write("author: \""+this.author+"\"\n");
			os.write("description: \""+this.description+"\"\n");
			
			os.newLine();
			
			for(int t = 0;t < this.targets.size();t++){
				BendsTarget target = this.targets.get(t);
				os.write("target " + target.mob + " {\n");
				String anim = null;
				for(int a = 0;a < target.actions.size();a++){
					BendsAction action = target.actions.get(a);
					if(action.calculations.size() > 0){
						if(anim == null || !anim.equalsIgnoreCase(action.anim)){
							if(anim != null)
								os.write(tab+"}\n");
							os.write(tab+"anim "+action.anim+" {\n");
							anim = action.anim;
						}
						
						os.write(tab+tab+"@"+action.model+":"+(action.prop == EnumBoxProperty.ROT ? "rot" : action.prop == EnumBoxProperty.SCALE ? "scale" : "prerot") + ":" + (action.axis == EnumAxis.X ? "x" : action.axis == EnumAxis.Y ? "y" : action.axis == EnumAxis.Z ? "z" : "")+" ");
						
						for(int c = 0;c < action.calculations.size();c++){
							Calculation calc = action.calculations.get(c);
							os.write((calc.operator == EnumOperator.SET ? "==" : calc.operator == EnumOperator.ADD ? "+=" : calc.operator == EnumOperator.SUBSTRACT ? "-=" : calc.operator == EnumOperator.MULTIPLY ? "*=" : calc.operator == EnumOperator.DIVIDE ? "/=" : "=="));
							if(c == 0){
								os.write(" " + (action.mod == EnumModifier.COS ? ":cos:" : action.mod == EnumModifier.SIN ? ":sin:" : ""));
							}
							os.write(calc.globalVar == null ? (""+calc.number) : ("$"+calc.globalVar));
						}
						os.write(" #"+action.smooth);
						os.newLine();
						
						if(a == target.actions.size()-1)
							os.write(tab+"}\n");
					}
				}
				os.write("}\n\n");
			}
			
			os.close();
	}
	
	public static String constructFilenameWithDisplayName(String argName){
		String filename = argName;
		filename = filename.toLowerCase();
		filename = filename.replace('.', ' ');
		filename = filename.trim();
		filename = filename.replace(" ", "_");
		return filename+".bends";
	}
}
