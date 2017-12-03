package net.gobbob.mobends.pack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.main.ModBase;
import net.gobbob.mobends.main.ModStatics;
import net.gobbob.mobends.pack.BendsAction.Calculation;
import net.gobbob.mobends.pack.BendsAction.EnumBoxProperty;
import net.gobbob.mobends.pack.BendsAction.EnumModifier;
import net.gobbob.mobends.pack.BendsAction.EnumOperator;
import net.gobbob.mobends.util.EnumAxis;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;

public class BendsPack {
	public static final ResourceLocation DEFAULT_THUMBNAIL_TEXTURE = new ResourceLocation(ModStatics.MODID,"textures/gui/default_pack_thumbnail.png");
	public static HashMap<String, BendsTarget> targets = new HashMap<String, BendsTarget>();
	
	private String filename;
	private String displayName;
	private String author;
	private String description;
	private URL downloadURL;
	private String thumbnailURL;
	private ResourceLocation thumbnailLocation;
	
	public BendsPack(){}
	
	public BendsPack(String filename, String displayName, String author, String description) {
		this.filename = filename;
		this.displayName = displayName;
		this.author = author;
		this.description = description;
		if(this.filename == null)
			this.filename = constructName(displayName);
	}

	public void readBasicInfo(File file) throws IOException{
		this.filename = file.getName();
		
		BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
		
		String line = reader.readLine();
		while(line != null){
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
	
	public void apply() {
		targets.clear();
		if(filename == null)
			return;
		
		System.out.println("Applying a pack.");
		
		BufferedReader reader = null;
		boolean cachePack = false;
		if(downloadURL != null) {
			boolean downloadPack = PackManager.cachedDatabase.getEntry(filename) == null ||
					PackManager.cachedDatabase.getEntry(filename).get("updatedDate") != PackManager.publicDatabase.getEntry(filename).get("updatedDate");
			
			if(!downloadPack) {
				try {
					reader = new BufferedReader(new FileReader(new File(PackManager.cacheDirectory, filename)));
					System.out.println("Reading a cached public pack.");
				} catch (FileNotFoundException e) {
					downloadPack = true;
				}
			}
			
			if(downloadPack){
				PackManager.cachedDatabase.addEntry(filename).set("updatedDate", PackManager.publicDatabase.getEntry(filename).get("updatedDate"));
				try {
					reader = new BufferedReader(new InputStreamReader(downloadURL.openStream()));
					System.out.println("Downloading a public pack.");
				} catch (IOException e) { e.printStackTrace(); }
				PackManager.cachedDatabase.saveToFile(PackManager.databaseCacheFile);
				cachePack = true;
			}
		}else{
			try {
				reader = new BufferedReader(new FileReader(new File(PackManager.localDirectory, filename)));
				System.out.println("Reading a local pack.");
			} catch (FileNotFoundException e) { e.printStackTrace(); }
		}
		
		String[] lines = new String[0];
		if(reader != null) {
			lines = GUtil.readLines(reader);
		}
		if(cachePack) {
			GUtil.writeLines(new File(PackManager.cacheDirectory, filename), lines);
		}
		
		String anim = "";
		BendsTarget target = null;
		for(String line : lines) {
			if(line.startsWith("target")){
				String data = line;
				data = formatStringData("target", data);
				target = new BendsTarget(data.toLowerCase());
				targets.put(data.toLowerCase(), target);
			}else if(line.contains("anim")){
				String data = line;
				anim = formatStringData("anim", data).trim();
				if(target != null && !target.conditions.containsKey(anim)) {
					target.conditions.put(anim, new BendsCondition(anim));
				}
			}else if(line.contains("@")){
				if(target != null){
					target.getCondition(anim).addAction(getActionFromLine(line));
				}
			}
		}
	}
	
	public void downloadThumbnail() {
		final ResourceLocation resourcelocation = new ResourceLocation(ModStatics.MODID, "bendsPackThumbnails/" + filename);
        ITextureObject itextureobject = Minecraft.getMinecraft().getTextureManager().getTexture(resourcelocation);
        
        if (itextureobject == null)
        {
            File file2 = new File(PackManager.cacheDirectory, filename+".png");
            final IImageBuffer iimagebuffer = new ImageBufferDownload();
            
            ThreadDownloadImageData threaddownloadimagedata = new ThreadDownloadImageData(file2, thumbnailURL, DEFAULT_THUMBNAIL_TEXTURE, null);
            Minecraft.getMinecraft().getTextureManager().loadTexture(resourcelocation, threaddownloadimagedata);
        }

        this.thumbnailLocation = resourcelocation;
	}
	
	public void save() throws IOException{
		if(isPublic()) return;
		
		System.out.println("Saving " + this.filename);
		
		final String tab = "	";
		
		if(this.filename == null){
			this.filename = constructName(this.displayName);
		}
		
		File packFile = new File(PackManager.localDirectory,this.filename+"");
		packFile.createNewFile();
		
		final BufferedWriter os = new BufferedWriter(new FileWriter(packFile));
		
		os.write("name: \""+this.displayName+"\"\n");
		os.write("author: \""+this.author+"\"\n");
		os.write("description: \""+this.description+"\"\n");
		
		os.newLine();
		for(BendsTarget target : targets.values()) {
			try {
				os.write("target " + target.mob + " {\n");
				for(BendsCondition condition : target.conditions.values()) {
					try {
						os.write(tab+"anim "+condition.getAnimationName()+" {\n");
						for(int a = 0; a < condition.getActionAmount(); a++) {
							BendsAction action = condition.getAction(a);
							os.write(tab+tab+"@"+action.model+":"+(action.property == EnumBoxProperty.ROT ? "rot" : action.property == EnumBoxProperty.SCALE ? "scale" : "prerot") + ":" + (action.axis == EnumAxis.X ? "x" : action.axis == EnumAxis.Y ? "y" : action.axis == EnumAxis.Z ? "z" : "")+" ");
							
							for(int c = 0;c < action.calculations.size();c++){
								Calculation calc = action.calculations.get(c);
								os.write((calc.operator == EnumOperator.SET ? "==" : calc.operator == EnumOperator.ADD ? "+=" : calc.operator == EnumOperator.SUBSTRACT ? "-=" : calc.operator == EnumOperator.MULTIPLY ? "*=" : calc.operator == EnumOperator.DIVIDE ? "/=" : "=="));
								if(c == 0){
									os.write(" ");
									if(action.modifier != null) {
										os.write(":" + action.modifier.name().toLowerCase() + ":");
									}
								}
								os.write(calc.globalVar == null ? (""+calc.number) : ("$"+calc.globalVar));
							}
							os.write(" #"+action.smooth);
							os.newLine();
						}
						os.write(tab+"}\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				os.write("}\n\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		os.close();
	}
	
	public void saveBasicInfo() {
		if(isPublic()) return;
		
		final String tab = "	";
		if(this.filename == null){
			this.filename = constructName(this.displayName);
		}
		File packFile = new File(PackManager.localDirectory,this.filename+"");
		try {
			String animationInfo = "";
			if(packFile.exists()) {
				animationInfo = GUtil.readFile(packFile);
				int index = animationInfo.indexOf("\ntarget");
				if(index >= 0)
					animationInfo = animationInfo.substring(index);
				else{
					animationInfo = "";
				}
			}else{
				packFile.createNewFile();
			}
			final BufferedWriter os = new BufferedWriter(new FileWriter(packFile));
			os.write("name: \""+this.displayName+"\"\n");
			os.write("author: \""+this.author+"\"\n");
			os.write("description: \""+this.description+"\"\n");
			os.write(animationInfo);
			
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void rename(String newName) {
		if(this.filename.equalsIgnoreCase(newName) || isPublic())
			return;
		
		this.filename = newName;
	}
	
	public BendsPack setThumbnailURL(String string) {
		thumbnailURL = string;
		if(thumbnailURL != null) downloadThumbnail();
		return this;
	}
	
	public BendsPack setDownloadURL(String string) {
		try {
			downloadURL = new URL(string);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return this;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public ResourceLocation getThumbnail() {
		return thumbnailLocation != null ? thumbnailLocation : DEFAULT_THUMBNAIL_TEXTURE;
	}
	
	public boolean isPublic() {
		return downloadURL != null;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static String constructName(String argName){
		String name = argName;
		name = name.toLowerCase();
		name = name.replace('.', ' ');
		name = name.trim();
		name = name.replace(" ", "_");
		return name;
	}

	public static BendsTarget createTarget(String id) {
		BendsTarget target = new BendsTarget(id);
		targets.put(id, target);
		return target;
	}
	
	public static String formatStringData(String header,String data){
		data = data.replaceFirst(header, "");
		if(data.contains("\"")) data = data.replaceAll("\"", "");
		if(data.contains("{")) data = data.replace("{", "");
		data = data.trim();
		return data;
	}
	
	/* 
	 * Structure used to temporarily store parsed info about an action.
	 */
	private static class Operation{
		public String operator = "";
		public String num = "";
		public String globalVar = null;
		
		public Operation() {}
	}
	
	public static BendsAction getActionFromLine(String line){
		BendsAction action = new BendsAction();
		action.model = "";
		
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
		
		for(int i = 0;i < calcs.size();i++){
			calcs.get(i).num = calcs.get(i).num.trim();
			
			for(EnumModifier modifier : EnumModifier.values()) {
				if(calcs.get(i).num.contains(":" + modifier.name().toLowerCase() + ":")){
					action.modifier = modifier;
					calcs.get(i).num = calcs.get(i).num.replaceAll(":" + modifier.name().toLowerCase() + ":","");
					calcs.get(i).num = calcs.get(i).num.trim();
					break;
				}
			}
			
			if(calcs.get(i).num.contains("$")){
				calcs.get(i).num = calcs.get(i).num.replace("$", " ");
				calcs.get(i).num = calcs.get(i).num.trim();
				calcs.get(i).globalVar = calcs.get(i).num;
				calcs.get(i).num = "0";
			}
			calcs.get(i).operator = calcs.get(i).operator.trim();
			
			if(!calcs.get(i).num.startsWith("#"))
				action.calculations.add(new Calculation(BendsAction.getOperatorFromSymbol(calcs.get(i).operator),Float.parseFloat(calcs.get(i).num)).setGlobalVar(calcs.get(i).globalVar));
		}
		
		if(line.contains(":rot:"))
			action.property = EnumBoxProperty.ROT;
		else if(line.contains(":scale:"))
			action.property = EnumBoxProperty.SCALE;
		else if(line.contains(":prerot:"))
			action.property = EnumBoxProperty.PREROT;
		
		if(line.contains(":x"))
			action.axis = EnumAxis.X;
		else if(line.contains(":y"))
			action.axis = EnumAxis.Y;
		else if(line.contains(":z"))
			action.axis = EnumAxis.Z;
		
		action.smooth = smooth.isEmpty() ? 1 : Float.parseFloat(smooth);
		
		return action;
	}
	
	public static BendsTarget getTarget(String id){
		return (BendsTarget) targets.get(id);
	}
	
	public static void animate(IBendsModel model,String target,String anim){
		BendsTarget bTarget = getTarget(target);
		
		if(bTarget == null)
			return;
		
		bTarget.applyToModel(model, anim);
	}
}
