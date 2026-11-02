package goblinbob.mobends.core.supporters;

import goblinbob.mobends.core.asset.AssetLocation;
import goblinbob.mobends.core.util.Color;
import goblinbob.mobends.core.util.IColorRead;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AccessoryDetails
{
    private String displayName;
    /** The color it's shown in until the player picks one; null if the API doesn't say. */
    private Color defaultColor;
    private List<AccessoryPart> parts;

    public AccessoryDetails(String displayName, List<AccessoryPart> parts)
    {
        this.displayName = displayName;
        this.parts = parts;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    /** The color it's shown in until the player picks one; white if the API doesn't say. */
    public IColorRead getDefaultColor()
    {
        return defaultColor != null ? defaultColor : Color.WHITE;
    }

    public Iterable<AccessoryPart> getParts()
    {
        return parts;
    }

    public Collection<AssetLocation> getAssetLocations()
    {
        return parts.stream().flatMap(p -> p.getAssetLocations().stream()).collect(Collectors.toCollection(ArrayList::new));
    }
}
