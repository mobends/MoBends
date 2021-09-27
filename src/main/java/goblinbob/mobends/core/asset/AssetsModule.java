package goblinbob.mobends.core.asset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.module.IModule;
import goblinbob.mobends.core.util.ConnectionHelper;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.http.conn.HttpHostConnectException;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static goblinbob.mobends.core.util.ConnectionHelper.sendGetRequest;

public class AssetsModule
{
    public static AssetsModule INSTANCE;

    private final String apiUrl;
    private final File assetsDirectory;
    private final File localManifestFile;

    private AssetManifest localManifest;

    public AssetsModule(File configDirectory)
    {
        this.apiUrl = EnvironmentModule.getConfig().getApiUrl();
        File modConfigDirectory = new File(configDirectory, "mobends");
        this.assetsDirectory = new File(modConfigDirectory, "assets");
        this.assetsDirectory.mkdirs();

        this.localManifestFile = new File(modConfigDirectory, "asset_manifest.json");

        this.updateAssets();
    }

    private void fetchLocalManifest()
    {
        this.localManifest = null;

        if (this.localManifestFile.isFile())
        {
            Gson gson = ConnectionHelper.INSTANCE.getGson();

            try
            {
                this.localManifest = gson.fromJson(new BufferedReader(new FileReader(this.localManifestFile)), AssetManifest.class);
            }
            catch (JsonParseException | FileNotFoundException e)
            {
                Core.LOG.warning("Failed to get local asset manifest.");
                e.printStackTrace();
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
        try (FileWriter writer = new FileWriter(localManifestFile))
        {
            Gson gson = ConnectionHelper.INSTANCE.getGson();
            gson.toJson(manifest, writer);
        }
        catch (JsonParseException | IOException e)
        {
            Core.LOG.warning("Failed to save local asset manifest.");
            e.printStackTrace();
        }

        this.localManifest = manifest;
    }

    private void downloadAsset(AssetManifest manifest, AssetDefinition asset) throws MalformedAssetException
    {
        try
        {
            URL url = new URL(manifest.getBaseUrl() + asset.getPath().getAssetPath());
            URLConnection connection = url.openConnection();
            DataInputStream dis = new DataInputStream(connection.getInputStream());
            byte[] fileData = new byte[connection.getContentLength()];
            for (int q = 0; q < fileData.length; q++)
            {
                fileData[q] = dis.readByte();
            }
            dis.close();

            // Making sure the path exists.
            File localAssetPath = getAssetFile(asset.getPath());
            localAssetPath.getParentFile().mkdirs();
            // Saving the file.
            FileOutputStream fos = new FileOutputStream(localAssetPath);
            fos.write(fileData);
            fos.close();
        }
        catch (IOException m)
        {
            throw new MalformedAssetException(String.format("Couldn't download asset: %s", asset.getPath()), m);
        }
    }

    public void updateAssets()
    {
        fetchLocalManifest();

        AssetManifest onlineManifest = fetchOnlineManifest();

        if (onlineManifest == null)
        {
            return;
        }

        if (localManifest != null && onlineManifest.getVersion() <= localManifest.getVersion())
        {
            // No new updates.
            return;
        }

        Core.LOG.info("New assets detected");
        Iterable<AssetDefinition> assetsToUpdate = AssetManifest.getAssetsToUpdate(localManifest, onlineManifest);

        try
        {
            for (AssetDefinition asset : assetsToUpdate)
            {
                downloadAsset(onlineManifest, asset);
            }

            // If we fail to download an asset, this isn't gonna get called.
            this.storeManifestLocally(onlineManifest);
        }
        catch(MalformedAssetException e)
        {
            Core.LOG.warning(e.getMessage());
        }
    }

    public Collection<AssetDefinition> getAssets()
    {
        return localManifest != null ? localManifest.getAssets() : Collections.emptyList();
    }

    public File getAssetFile(AssetLocation location)
    {
        return new File(assetsDirectory, location.getAssetPath());
    }

    public static class Factory implements IModule
    {
        @Override
        public void preInit(FMLPreInitializationEvent event)
        {
            AssetsModule.INSTANCE = new AssetsModule(event.getModConfigurationDirectory());
        }

        @Override
        public void onRefresh()
        {
            AssetsModule.INSTANCE.updateAssets();
        }
    }
}
