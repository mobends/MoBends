package goblinbob.mobends.core.asset;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.module.IModule;
import goblinbob.mobends.core.util.ConnectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.io.IOUtils;
import org.apache.http.conn.HttpHostConnectException;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static goblinbob.mobends.core.util.ConnectionHelper.sendGetRequest;

/**
 * The downloadable assets (supporter accessories): the ones on disk are registered on every
 * resource reload; new ones are downloaded in the background, then registered.
 */
public class AssetsModule
{
    public static AssetsModule INSTANCE;

    private static final int TIMEOUT_MILLIS = 10000;

    private final String apiUrl;
    private final File assetsDirectory;
    private final File localManifestFile;

    /** Read by the client thread, replaced by the download thread. */
    private volatile AssetManifest localManifest;
    private volatile boolean updating;

    public AssetsModule(File configDirectory)
    {
        this.apiUrl = EnvironmentModule.getConfig().getApiUrl();
        File modConfigDirectory = new File(configDirectory, "mobends");
        this.assetsDirectory = new File(modConfigDirectory, "assets");
        this.assetsDirectory.mkdirs();

        this.localManifestFile = new File(modConfigDirectory, "asset_manifest.json");

        fetchLocalManifest();
    }

    private void fetchLocalManifest()
    {
        this.localManifest = null;

        if (this.localManifestFile.isFile())
        {
            Gson gson = ConnectionHelper.INSTANCE.getGson();

            try (Reader reader = new InputStreamReader(new FileInputStream(this.localManifestFile), StandardCharsets.UTF_8))
            {
                this.localManifest = gson.fromJson(reader, AssetManifest.class);
            }
            catch (JsonParseException | IOException e)
            {
                Core.LOG.log(java.util.logging.Level.WARNING, "Failed to get local asset manifest.", e);
            }
        }
    }

    private AssetManifest fetchOnlineManifest()
    {
        Map<String, String> params = new HashMap<>();

        try
        {
            return sendGetRequest(new URL(apiUrl + "/api/asset/manifest"), params, AssetManifest.class);
        }
        catch (HttpHostConnectException e)
        {
            // No internet, do nothing.
        }
        catch(JsonParseException e)
        {
            Core.LOG.warning("Failed to parse online asset manifest.");
            e.printStackTrace();
        }
        catch(IOException|URISyntaxException e)
        {
            Core.LOG.warning("Failed to get online asset manifest.");
            e.printStackTrace();
        }

        return null;
    }

    private void storeManifestLocally(AssetManifest manifest)
    {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(localManifestFile), StandardCharsets.UTF_8))
        {
            Gson gson = ConnectionHelper.INSTANCE.getGson();
            gson.toJson(manifest, writer);
        }
        catch (JsonParseException | IOException e)
        {
            Core.LOG.log(java.util.logging.Level.WARNING, "Failed to save local asset manifest.", e);
        }

        this.localManifest = manifest;
    }

    private void downloadAsset(AssetManifest manifest, AssetDefinition asset) throws MalformedAssetException
    {
        try
        {
            File localAssetPath = getAssetFile(asset.getPath());
            URLConnection connection = new URL(manifest.getBaseUrl() + asset.getPath().getAssetPath()).openConnection();
            connection.setConnectTimeout(TIMEOUT_MILLIS);
            connection.setReadTimeout(TIMEOUT_MILLIS);
            byte[] fileData;
            try (InputStream stream = connection.getInputStream())
            {
                fileData = IOUtils.toByteArray(stream);
            }

            localAssetPath.getParentFile().mkdirs();
            try (OutputStream out = new FileOutputStream(localAssetPath))
            {
                out.write(fileData);
            }
        }
        catch (IOException m)
        {
            throw new MalformedAssetException(String.format("Couldn't download asset: %s", asset.getPath()), m);
        }
    }

    /** Downloads new assets on a background thread, then registers them on the client thread. */
    public void updateAssetsInBackground()
    {
        if (updating)
        {
            return;
        }
        updating = true;
        Thread thread = new Thread(() -> {
            try
            {
                if (updateAssets())
                {
                    Minecraft.getMinecraft().addScheduledTask(AssetReloadListener::registerAssets);
                }
            }
            finally
            {
                updating = false;
            }
        }, "Mo' Bends asset download");
        thread.setDaemon(true);
        thread.start();
    }

    /** @return whether new assets were downloaded */
    private boolean updateAssets()
    {
        AssetManifest onlineManifest = fetchOnlineManifest();

        if (onlineManifest == null)
        {
            return false;
        }

        Iterable<AssetDefinition> assetsToUpdate = AssetManifest.getAssetsToUpdate(localManifest, onlineManifest);
        if (!assetsToUpdate.iterator().hasNext())
        {
            return false;
        }

        Core.LOG.info("New assets detected");
        try
        {
            for (AssetDefinition asset : assetsToUpdate)
            {
                downloadAsset(onlineManifest, asset);
            }

            // If we fail to download an asset, this isn't gonna get called.
            this.storeManifestLocally(onlineManifest);
            return true;
        }
        catch(MalformedAssetException e)
        {
            Core.LOG.warning(e.getMessage());
            return false;
        }
    }

    public Collection<AssetDefinition> getAssets()
    {
        AssetManifest manifest = localManifest;
        return manifest != null ? manifest.getAssets() : Collections.emptyList();
    }

    /**
     * The local file of an asset. The paths come from the remote manifest, so one that would lead
     * out of the assets folder is refused.
     */
    public File getAssetFile(AssetLocation location) throws IOException
    {
        File file = new File(assetsDirectory, location.getAssetPath());
        if (!file.getCanonicalPath().startsWith(assetsDirectory.getCanonicalPath() + File.separator))
        {
            throw new IOException("The asset path leads out of the assets folder: " + location.getAssetPath());
        }
        return file;
    }

    public static class Factory implements IModule
    {
        @Override
        public void preInit(FMLPreInitializationEvent event)
        {
            AssetsModule.INSTANCE = new AssetsModule(event.getModConfigurationDirectory());
            AssetsModule.INSTANCE.updateAssetsInBackground();
        }

        @Override
        public void onRefresh()
        {
            AssetsModule.INSTANCE.updateAssetsInBackground();
        }
    }
}
