package net.gobbob.mobends.core.pack;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.gobbob.mobends.core.util.BendsPackHelper;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LocalBendsPack implements IBendsPack
{

	private BendsPackMeta metadata;
	private ResourceLocation thumbnailLocation = ThumbnailProvider.DEFAULT_THUMBNAIL_LOCATION;

	public LocalBendsPack(String displayName, String author, String description) {
		this.metadata = new BendsPackMeta();
		this.metadata.key = BendsPackHelper.constructPackName(displayName);
		this.metadata.displayName = displayName;
		this.metadata.author = author;
		this.metadata.description = description;
	}

	public LocalBendsPack(BendsPackMeta metadata)
	{
		this.metadata = metadata;
	}

	public void rename(String newName)
	{
		if (this.metadata.key.equals(newName))
			return;

		this.metadata.key = newName;
	}

	public LocalBendsPack setThumbnailURL(String thumbnailURL)
	{
		this.thumbnailLocation = PackManager.instance.getThumbnailLocation(this.metadata.key, thumbnailURL);
		return this;
	}

	@Override
	public String getKey()
	{
		return metadata.key;
	}

	@Override
	public String getDisplayName()
	{
		return metadata.displayName;
	}

	@Override
	public String getAuthor()
	{
		return metadata.author;
	}

	@Override
	public String getDescription()
	{
		return metadata.description;
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

	public static LocalBendsPack readFromFile(File file) throws IOException
	{
		JsonReader fileReader = new JsonReader(new FileReader(file));
		BendsPackMeta meta = (new Gson()).fromJson(fileReader, BendsPackMeta.class);
		return new LocalBendsPack(meta);
	}

}
