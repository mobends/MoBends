package net.gobbob.mobends.core.pack;

import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;

import java.io.File;

public class ThumbnailProvider
{

    public static final ResourceLocation DEFAULT_THUMBNAIL_LOCATION = new ResourceLocation(ModStatics.MODID,
            "textures/gui/default_pack_thumbnail.png");

    private File cacheDirectory;

    public ThumbnailProvider(File cacheDirectory)
    {
        this.cacheDirectory = cacheDirectory;
    }

    public ResourceLocation getThumbnailLocation(String packName, String thumbnailUrl)
    {
        final ResourceLocation resourceLocation = new ResourceLocation(ModStatics.MODID,
                "bendsPackThumbnails/" + packName);
        ITextureObject itextureobject = Minecraft.getMinecraft().getTextureManager().getTexture(resourceLocation);

        if (itextureobject == null)
        {
            File file = new File(cacheDirectory, packName + ".png");

            ThreadDownloadImageData threaddownloadimagedata = new ThreadDownloadImageData(file, thumbnailUrl,
                    DEFAULT_THUMBNAIL_LOCATION, null);
            Minecraft.getMinecraft().getTextureManager().loadTexture(resourceLocation, threaddownloadimagedata);
        }

        return resourceLocation;
    }

}
