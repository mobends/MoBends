package goblinbob.mobends.core.asset;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class AssetTexture extends AbstractTexture
{
    private static final Logger LOGGER = LogManager.getLogger();

    private final AssetLocation assetLocation;

    @Nullable
    private BufferedImage bufferedImage;
    private boolean textureUploaded = false;

    public AssetTexture(AssetLocation assetLocation)
    {
        this.assetLocation = assetLocation;
    }

    private void checkTextureUploaded()
    {
        if (!this.textureUploaded)
        {
            if (this.bufferedImage != null)
            {
                TextureUtil.uploadTextureImage(super.getGlTextureId(), this.bufferedImage);
                this.textureUploaded = true;
            }
        }
    }

    @Override
    public int getGlTextureId()
    {
        this.checkTextureUploaded();
        return super.getGlTextureId();
    }

    @Override
    public void loadTexture(IResourceManager resourceManager) throws IOException
    {
        try
        {
            this.bufferedImage = ImageIO.read(AssetsModule.INSTANCE.getAssetFile(assetLocation));
        }
        catch (IOException ioexception)
        {
            LOGGER.error("Couldn't load asset texture {}", assetLocation.toString(), ioexception);
            throw ioexception;
        }
    }
}
