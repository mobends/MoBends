package goblinbob.mobends.core.supporters;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A part of an accessory, as {@code /api/accessory/details} describes it: a model worn at a bind
 * point, drawn with its diffuse texture, then again with its inked texture in the colour the
 * player picked. The paths are of downloaded assets ({@code models/...json}, {@code textures/...png},
 * see {@code AssetsModule}).
 *
 * @param bindPoint null if the API names one this version doesn't know: the part isn't drawn
 */
public record AccessoryPart(String key, @Nullable BindPoint bindPoint, @Nullable String modelPath, @Nullable String diffuseTexturePath,
                            @Nullable String inkedTexturePath) {
    /** The assets the part is drawn with. */
    public List<String> assetPaths() {
        List<String> paths = new ArrayList<>(3);
        for (String path : new String[] {modelPath, diffuseTexturePath, inkedTexturePath}) {
            if (path != null) {
                paths.add(path);
            }
        }
        return paths;
    }
}
