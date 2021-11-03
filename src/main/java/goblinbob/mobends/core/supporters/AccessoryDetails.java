package goblinbob.mobends.core.supporters;

import goblinbob.mobends.core.asset.AssetLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AccessoryDetails
{
    private String displayName;
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

    public Iterable<AccessoryPart> getParts()
    {
        return parts;
    }

    public Collection<AssetLocation> getAssetLocations()
    {
        return parts.stream().flatMap(p -> p.getAssetLocations().stream()).collect(Collectors.toCollection(ArrayList::new));
    }
}
