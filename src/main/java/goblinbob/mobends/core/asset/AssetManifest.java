package goblinbob.mobends.core.asset;

import java.util.*;

public class AssetManifest
{
    private int version = 0;
    private String baseUrl = "";
    private List<AssetDefinition> assets = new ArrayList<>();

    public int getVersion()
    {
        return version;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public Collection<AssetDefinition> getAssets()
    {
        return Collections.unmodifiableCollection(assets);
    }

    public static Iterable<AssetDefinition> getAssetsToUpdate(AssetManifest oldManifest, AssetManifest newManifest)
    {
        if (oldManifest == null)
        {
            return newManifest.getAssets();
        }

        List<AssetDefinition> assetsToUpdate = new ArrayList<>();
        Map<Integer, Integer> versionMap = new HashMap<>();

        for (AssetDefinition asset : oldManifest.getAssets())
        {
            versionMap.put(asset.getId(), asset.getVersion());
        }

        for (AssetDefinition asset : newManifest.getAssets())
        {
            int assetId = asset.getId();

            if (!versionMap.containsKey(assetId) || versionMap.get(assetId) < asset.getVersion())
            {
                assetsToUpdate.add(asset);
            }
        }

        return assetsToUpdate;
    }
}
