package net.gobbob.mobends.core.pack;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.gobbob.mobends.core.Core;
import net.gobbob.mobends.core.client.model.IBendsModel;
import net.gobbob.mobends.core.pack.BendsAction.Calculation;
import net.gobbob.mobends.core.pack.BendsAction.EnumBoxProperty;
import net.gobbob.mobends.core.util.EnumAxis;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;

public class BendsPack
{
	public static final ResourceLocation DEFAULT_THUMBNAIL_TEXTURE = new ResourceLocation(ModStatics.MODID,
			"textures/gui/default_pack_thumbnail.png");
	public static HashMap<String, BendsTarget> targets = new HashMap<String, BendsTarget>();

	private String filename;
	private String displayName;
	private String author;
	private String description;
	private URL downloadURL;
	private String thumbnailURL;
	private ResourceLocation thumbnailLocation;

	public BendsPack()
	{
	}

	public BendsPack(String filename, String displayName, String author, String description)
	{
		this.filename = filename;
		this.displayName = displayName;
		this.author = author;
		this.description = description;
		if (this.filename == null)
			this.filename = constructName(displayName);
	}

	public void readBasicInfo(File file) throws IOException
	{
		this.filename = file.getName();

		BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));

		String line = reader.readLine();
		while (line != null)
		{
			if (line.startsWith("name:"))
			{
				String data = line;
				this.displayName = formatStringData("name:", data);
			}
			else if (line.startsWith("author:"))
			{
				String data = line;
				this.author = formatStringData("author:", data);
			}
			else if (line.startsWith("description:"))
			{
				String data = line;
				this.description = formatStringData("description:", data);
			}

			line = reader.readLine();
		}

		reader.close();
	}

	public void apply()
	{
		targets.clear();
		if (filename == null)
			return;

		System.out.println("Applying a pack.");

		BufferedReader reader = null;
		boolean cachePack = false;
		if (downloadURL != null)
		{
			boolean downloadPack = PackManager.cachedDatabase.getEntry(filename) == null
					|| PackManager.cachedDatabase.getEntry(filename).get("updatedDate") != PackManager.publicDatabase
							.getEntry(filename).get("updatedDate");

			if (!downloadPack)
			{
				try
				{
					reader = new BufferedReader(new FileReader(new File(PackManager.cacheDirectory, filename)));
					System.out.println("Reading a cached public pack.");
				}
				catch (FileNotFoundException e)
				{
					downloadPack = true;
				}
			}

			if (downloadPack)
			{
				PackManager.cachedDatabase.addEntry(filename).set("updatedDate",
						PackManager.publicDatabase.getEntry(filename).get("updatedDate"));
				try
				{
					reader = new BufferedReader(new InputStreamReader(downloadURL.openStream()));
					System.out.println("Downloading a public pack.");
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				PackManager.cachedDatabase.saveToFile(PackManager.databaseCacheFile);
				cachePack = true;
			}
		}
		else
		{
			try
			{
				reader = new BufferedReader(new FileReader(new File(PackManager.localDirectory, filename)));
				System.out.println("Reading a local pack.");
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}

		String[] lines = new String[0];
		if (reader != null)
		{
			lines = GUtil.readLines(reader);
		}
		
		if (cachePack)
		{
			GUtil.writeLines(new File(PackManager.cacheDirectory, filename), lines);
		}

		String anim = "";
		BendsTarget target = null;
		for (String line : lines)
		{
			if (line.startsWith("target"))
			{
				String data = line;
				data = formatStringData("target", data);
				target = new BendsTarget(data.toLowerCase());
				targets.put(data.toLowerCase(), target);
			}
			else if (line.contains("anim"))
			{
				String data = line;
				anim = formatStringData("anim", data).trim();
				if (target != null && !target.conditions.containsKey(anim))
				{
					target.conditions.put(anim, new BendsCondition(anim));
				}
			}
			else if (line.contains("@"))
			{
				if (target != null)
				{
					target.getCondition(anim).addAction(getActionFromLine(line));
				}
			}
		}
	}

	public void downloadThumbnail()
	{
		final ResourceLocation resourcelocation = new ResourceLocation(ModStatics.MODID,
				"bendsPackThumbnails/" + filename);
		ITextureObject itextureobject = Minecraft.getMinecraft().getTextureManager().getTexture(resourcelocation);

		if (itextureobject == null)
		{
			File file = new File(PackManager.cacheDirectory, filename + ".png");

			ThreadDownloadImageData threaddownloadimagedata = new ThreadDownloadImageData(file, thumbnailURL,
					DEFAULT_THUMBNAIL_TEXTURE, null);
			Minecraft.getMinecraft().getTextureManager().loadTexture(resourcelocation, threaddownloadimagedata);
		}

		this.thumbnailLocation = resourcelocation;
	}

	public void save() throws IOException
	{
		if (isPublic())
			return;

		System.out.println("Saving " + this.filename);

		if (this.filename == null)
		{
			this.filename = constructName(this.displayName);
		}

		File packFile = new File(PackManager.localDirectory, this.filename);
		packFile.createNewFile();

		StringBuilder sb = new StringBuilder();
		sb.append("name: \"").append(this.displayName).append("\"\n");
		sb.append("author: \"").append(this.author).append("\"\n");
		sb.append("description: \"").append(this.description).append("\"\n");

		sb.append('\n');
		for (BendsTarget target : targets.values())
		{
			sb.append("target ").append(target.name).append(" {\n");
			for (BendsCondition condition : target.conditions.values())
			{
				sb.append("\tanim ").append(condition.getAnimationName()).append(" {\n");
				for (BendsAction action : condition.getActions())
				{
					sb.append("\t\t@").append(action.part).append(':');
					switch (action.property)
					{
						case ROT:
							sb.append("rot");
							break;
						case SCALE:
							sb.append("scale");
							break;
						case PREROT:
							sb.append("prerot");
							break;
					}
					sb.append(':');
					// The axis is allowed to be null.
					if (action.axis != null)
					{
						switch (action.axis) {
							case X:
								sb.append('x');
								break;
							case Y:
								sb.append('y');
								break;
							case Z:
								sb.append('z');
								break;
						}
					}
					sb.append(' ');

					for (int c = 0; c < action.calculations.size(); c++)
					{
						Calculation calc = action.calculations.get(c);
						switch (calc.operator) {
						case SET:
							sb.append("==");
							break;
						case ADD:
							sb.append("+=");
							break;
						case SUBTRACT:
							sb.append("-=");
							break;
						case MULTIPLY:
							sb.append("*=");
							break;
						case DIVIDE:
							sb.append("/=");
							break;
						}

						if (c == 0)
						{
							sb.append(' ');
							if (action.modifier != null)
							{
								sb.append(":").append(action.modifier.name().toLowerCase()).append(":");
							}
						}
						sb.append(calc.globalVar == null ? calc.number : ("$" + calc.globalVar));
					}
					sb.append(" #").append(action.smooth).append('\n');
				}
				sb.append("\t}\n");
			}
			sb.append("}\n\n");
		}
		
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(packFile)))
		{
			writer.write(sb.toString());
		}
	}

	public void saveBasicInfo()
	{
		if (isPublic())
			return;

		if (this.filename == null)
		{
			this.filename = constructName(this.displayName);
		}
		File packFile = new File(PackManager.localDirectory, this.filename);

		String animationInfo = "";
		if (packFile.exists())
		{
			animationInfo = GUtil.readFile(packFile);
			int index = animationInfo.indexOf("\ntarget");
			if (index >= 0)
			{
				animationInfo = animationInfo.substring(index);
			}
			else
			{
				animationInfo = "";
			}
		}
		else
		{
			try
			{
				packFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
			
		StringBuilder sb = new StringBuilder();
		
		sb.append("name: \"").append(this.displayName).append("\"\n");
		sb.append("author: \"").append(this.author).append("\"\n");
		sb.append("description: \"").append(this.description).append("\"\n");
		sb.append(animationInfo);

		try (BufferedWriter os = new BufferedWriter(new FileWriter(packFile)))
		{
			os.write(sb.toString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void rename(String newName)
	{
		if (this.filename.equalsIgnoreCase(newName) || isPublic())
			return;

		this.filename = newName;
	}

	public BendsPack setThumbnailURL(String string)
	{
		thumbnailURL = string;
		if (thumbnailURL != null)
			downloadThumbnail();
		return this;
	}

	public BendsPack setDownloadURL(String string)
	{
		try
		{
			downloadURL = new URL(string);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public ResourceLocation getThumbnail()
	{
		return thumbnailLocation != null ? thumbnailLocation : DEFAULT_THUMBNAIL_TEXTURE;
	}

	public boolean isPublic()
	{
		return downloadURL != null;
	}

	public String getFilename()
	{
		return filename;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getAuthor()
	{
		return author;
	}

	public String getDescription()
	{
		return description;
	}

	public static String constructName(String argName)
	{
		String name = argName;
		name = name.toLowerCase();
		name = name.replace('.', ' ');
		name = name.trim();
		name = name.replace(" ", "_");
		return name;
	}

	public static BendsTarget createTarget(String id)
	{
		BendsTarget target = new BendsTarget(id);
		targets.put(id, target);
		return target;
	}

	public static String formatStringData(String header, String data)
	{
		data = data.replaceFirst(header, "");
		if (data.contains("\""))
			data = data.replaceAll("\"", "");
		if (data.contains("{"))
			data = data.replace("{", "");
		data = data.trim();
		return data;
	}

	/*
	 * Structure used to temporarily store parsed info about an action.
	 */
	private static class Operation
	{
		String operator = "";
		String num = "";
		String globalVar = null;
	}

	/**
	 * Format: @part:property:axis? operator value(OperatorValue)* #smoothness
	 * Examples:
	 *     @body:rot:x == 1.0*=5.0 #1.0
	 *     @rightArm:scale *= 2.0 #0.5
	 *     @leftArm:rot:z == 
	 * @param line
	 * @return
	 */
	public static BendsAction getActionFromLine(String line)
	{
		String trimmedLine = line.trim();
		
		if (!trimmedLine.startsWith("@"))
			throw new IllegalArgumentException("An action line should always start with a @ character.");
		
		String part = trimmedLine.substring(1, trimmedLine.indexOf(':'));
		trimmedLine = trimmedLine.substring(trimmedLine.indexOf(':') + 1);
		
		String propertyStr = trimmedLine.substring(0, trimmedLine.indexOf(':'));
		trimmedLine = trimmedLine.substring(trimmedLine.indexOf(':') + 1);
		EnumBoxProperty property = propertyStr.contentEquals("rot") ? EnumBoxProperty.ROT :
								   propertyStr.contentEquals("prerot") ? EnumBoxProperty.PREROT :
								   propertyStr.contentEquals("scale") ? EnumBoxProperty.SCALE : null;
		
		EnumAxis axis = trimmedLine.startsWith("x") ? EnumAxis.X :
						trimmedLine.startsWith("y") ? EnumAxis.Y :
						trimmedLine.startsWith("z") ? EnumAxis.Z : null;
		trimmedLine = trimmedLine.substring(1).trim(); // Getting rid of the possible axis
		
		String smoothStr = trimmedLine.substring(trimmedLine.lastIndexOf('#') + 1);
		float smooth = Float.parseFloat(smoothStr);
		trimmedLine = trimmedLine.substring(0, trimmedLine.lastIndexOf('#'));
		
		int calc = 0;

		List<Operation> calcs = new ArrayList<Operation>();
		calcs.add(new Operation());

		/*for (Operation op : calcs)
		{
			op.num = op.num.trim();

			for (EnumModifier modifier : EnumModifier.values())
			{
				if (op.num.contains(":" + modifier.name().toLowerCase() + ":"))
				{
					action.modifier = modifier;
					op.num = op.num.replaceAll(":" + modifier.name().toLowerCase() + ":", "");
					op.num = op.num.trim();
					break;
				}
			}

			if (op.num.contains("$"))
			{
				op.num = op.num.replace("$", " ");
				op.num = op.num.trim();
				op.globalVar = op.num;
				op.num = "0";
			}
			op.operator = op.operator.trim();

			if (!op.num.startsWith("#"))
				action.calculations.add(new Calculation(BendsAction.getOperatorFromSymbol(op.operator),
						Float.parseFloat(op.num)).setGlobalVar(op.globalVar));
		}*/

		return new BendsAction(part, property, axis, smooth);
	}

	public static BendsTarget getTarget(String id)
	{
		return targets.get(id);
	}

	public static void animate(IBendsModel model, String target, String anim)
	{
		BendsTarget bendsTarget = getTarget(target);

		if (bendsTarget != null)
			bendsTarget.applyToModel(model, anim);
	}

	public static void animate(IBendsModel model, String target, Collection<String> animations)
	{
		BendsTarget bendsTarget = getTarget(target);

		if (bendsTarget != null)
			bendsTarget.applyToModel(model, animations);
	}
}
