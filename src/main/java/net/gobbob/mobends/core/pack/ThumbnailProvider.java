package net.gobbob.mobends.core.pack;

import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;

public class ThumbnailProvider
{

    public static final ResourceLocation DEFAULT_THUMBNAIL_LOCATION = new ResourceLocation(ModStatics.MODID,
            "textures/gui/default_pack_thumbnail.png");

    private final PackCache packCache;

    public ThumbnailProvider(PackCache packCache)
    {
        this.packCache = packCache;
    }

    public ResourceLocation getThumbnailLocation(String packName, String thumbnailUrl)
    {
        final ResourceLocation resourceLocation = new ResourceLocation(ModStatics.MODID,
                "bendsPackThumbnails/" + packName);
        ITextureObject itextureobject = Minecraft.getMinecraft().getTextureManager().getTexture(resourceLocation);

        if (itextureobject == null)
        {
            ThreadDownloadImageData threaddownloadimagedata = new ThreadDownloadImageData(packCache.getThumbnailFile(packName), thumbnailUrl,
                    DEFAULT_THUMBNAIL_LOCATION, null);

            if (Minecraft.getMinecraft().getTextureManager().loadTexture(resourceLocation, threaddownloadimagedata))
            {
                return resourceLocation;
            }
        }

        return DEFAULT_THUMBNAIL_LOCATION;
    }

}
