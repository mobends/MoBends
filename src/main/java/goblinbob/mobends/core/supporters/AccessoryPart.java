package goblinbob.mobends.core.supporters;

import goblinbob.mobends.core.asset.AssetLocation;

import java.util.Arrays;
import java.util.Collection;

public class AccessoryPart
{
    private String key;
    private BindPoint bindPoint;
    private AssetLocation modelPath;
    private AssetLocation diffuseTexturePath;
    private AssetLocation inkedTexturePath;

    public String getKey()
    {
        return key;
    }

    public BindPoint getBindPoint()
    {
        return bindPoint;
    }

    public AssetLocation getModelPath()
    {
        return modelPath;
    }

    public AssetLocation getDiffuseTexturePath()
    {
        return diffuseTexturePath;
    }

    public AssetLocation getInkedTexturePath()
    {
        return inkedTexturePath;
    }

    public Collection<AssetLocation> getAssetLocations()
    {
        return Arrays.asList(modelPath, diffuseTexturePath, inkedTexturePath);
    }
}
