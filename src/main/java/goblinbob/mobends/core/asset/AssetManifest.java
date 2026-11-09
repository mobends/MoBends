package goblinbob.mobends.core.asset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The list of downloadable assets ({@code /api/asset/manifest}): each one's id, version and path,
 * and the URL the paths are under. The last one downloaded whole is kept in
 * {@code config/mobends/asset_manifest.json}.
 */
public record AssetManifest(String baseUrl, List<AssetDefinition> assets) {
    public AssetManifest {
        baseUrl = baseUrl == null ? "" : baseUrl;
        assets = assets == null ? List.of() : List.copyOf(assets.stream().filter(a -> a != null && a.path() != null).toList());
    }

    /** An asset: its id, its version (a newer one is downloaded again), and its path ({@code models/...json}, {@code textures/...png}). */
    public record AssetDefinition(int id, int version, String path) {
    }

    /** What's new or newer in {@code newManifest} than in {@code oldManifest} (all of it, if there's no old one). */
    public static List<AssetDefinition> getAssetsToUpdate(AssetManifest oldManifest, AssetManifest newManifest) {
        if (oldManifest == null) {
            return newManifest.assets();
        }
        Map<Integer, Integer> versions = new HashMap<>();
        for (AssetDefinition asset : oldManifest.assets()) {
            versions.put(asset.id(), asset.version());
        }
        List<AssetDefinition> toUpdate = new ArrayList<>();
        for (AssetDefinition asset : newManifest.assets()) {
            Integer version = versions.get(asset.id());
            if (version == null || version < asset.version()) {
                toUpdate.add(asset);
            }
        }
        return toUpdate;
    }
}
