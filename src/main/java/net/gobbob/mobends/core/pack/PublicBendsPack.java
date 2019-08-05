package net.gobbob.mobends.core.pack;

import net.minecraft.util.ResourceLocation;

import java.net.MalformedURLException;
import java.net.URL;

public class PublicBendsPack implements IBendsPack
{

    private String name;
    private String displayName;
    private String author;
    private String description;

    private URL downloadURL;
    private ResourceLocation thumbnailLocation;

    private PublicBendsPack(String name, String displayName, String author, String description)
    {
        this.name = name;
        this.displayName = displayName;
        this.author = author;
        this.description = description;
    }

    @Override
    public String getKey()
    {
        return name;
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
    public ResourceLocation getThumbnail()
    {
        return thumbnailLocation;
    }

    @Override
    public boolean canPackBeEdited()
    {
        return false;
    }

    private void setThumbnailURL(String thumbnailURL)
    {
        this.thumbnailLocation = PackManager.instance.getThumbnailLocation(this.name, thumbnailURL);
    }

    private void setDownloadURL(String downloadURL) throws MalformedURLException
    {
        this.downloadURL = new URL(downloadURL);
    }

    public static PublicBendsPack createPublicPack(PublicDatabase.PackEntry entry) throws MalformedURLException
    {
        PublicBendsPack pack = new PublicBendsPack(entry.name, entry.displayName, entry.author, entry.description);
        pack.setThumbnailURL(entry.thumbnail);
        pack.setDownloadURL(entry.downloadLink);
        return pack;
    }

}
