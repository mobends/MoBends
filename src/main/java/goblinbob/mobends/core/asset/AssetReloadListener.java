package goblinbob.mobends.core.asset;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import goblinbob.mobends.core.supporters.AccessoryDetails;
import goblinbob.mobends.core.supporters.SupporterContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.fml.common.ProgressManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collection;

public class AssetReloadListener implements IResourceManagerReloadListener
{
    private static final Logger LOGGER = LogManager.getLogger();

    public AssetReloadListener()
    {
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        // Refreshing assets
        AssetsModule.INSTANCE.updateAssets();

        AssetModels.INSTANCE.clearCache();

        Collection<AssetDefinition> assets = AssetsModule.INSTANCE.getAssets();

        ProgressManager.ProgressBar bar = net.minecraftforge.fml.common.ProgressManager.push("Reloading Mo' Bends Assets", assets.size(), true);

        for (AssetDefinition asset : assets)
        {
            AssetLocation location = asset.getPath();

            bar.step(location.toString());

            AssetType assetType = location.getAssetType();

            if (assetType == AssetType.TEXTURE)
            {
                AssetTexture assetTexture = new AssetTexture(location);

                if (!Minecraft.getMinecraft().getTextureManager().loadTexture(location, assetTexture))
                {
                    LOGGER.error("Couldn't upload asset texture: {}", location.toString());
                }
            }
            else if (assetType == AssetType.MODEL)
            {
                try
                {
                    AssetModels.INSTANCE.register(location);
                }
                catch (IOException | JsonSyntaxException e)
                {
                    LOGGER.error("Couldn't register asset model: {}", location.toString());
                    e.printStackTrace();
                }
            }
        }

        net.minecraftforge.fml.common.ProgressManager.pop(bar);
    }
}
