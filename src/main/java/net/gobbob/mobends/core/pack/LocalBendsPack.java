package net.gobbob.mobends.core.pack;

import net.gobbob.mobends.core.Core;
import net.gobbob.mobends.core.util.BendsPackHelper;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

public class LocalBendsPack implements IBendsPack
{

	private String filename;
	private String displayName;
	private String author;
	private String description;
	private ResourceLocation thumbnailLocation = null;

	public LocalBendsPack(String filename, String displayName, String author, String description)
	{
		this.filename = filename;
		this.displayName = displayName;
		this.author = author;
		this.description = description;
		if (this.filename == null)
			this.filename = BendsPackHelper.constructPackName(displayName);
	}

	public void save() throws IOException
	{
		if (this.filename == null)
		{
			this.filename = BendsPackHelper.constructPackName(this.displayName);
		}

		Core.LOG.info(String.format("Saving the '%s' bends pack.", this.filename));

		File packFile = PackManager.instance.getFileForPack(this.filename);
	}

	public void rename(String newName)
	{
		if (this.filename.equalsIgnoreCase(newName))
			return;

		this.filename = newName;
	}

	public LocalBendsPack setThumbnailURL(String thumbnailURL)
	{
		this.thumbnailLocation = PackManager.instance.getThumbnailLocation(this.filename, thumbnailURL);
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

	@Override
	public String getName()
	{
		return filename;
	}

	@Override
	public String getDisplayName()
	{
		return displayName;
	}

	@Override
	public String getAuthor()
	{
		return author;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	@Nullable
	public ResourceLocation getThumbnail()
	{
		return thumbnailLocation;
	}

	@Override
	public boolean canPackBeEdited()
	{
		return true;
	}

	public static LocalBendsPack readFromFile(File file)
	{
		LocalBendsPack pack = new LocalBendsPack("pack", "Pack", "Gobbob", "The best pack");
		return pack;
	}

}
