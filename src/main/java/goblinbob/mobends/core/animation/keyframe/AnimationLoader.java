package goblinbob.mobends.core.animation.keyframe;

import com.google.gson.Gson;
import goblinbob.mobends.core.client.PackTrust;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Loads keyframe clips (JSON, see misc/kumo-format.md, "Clips") from resources, caching them until the next reload. */
public class AnimationLoader
{

    private static final Map<ResourceLocation, KeyframeAnimation> cachedAnimations = new HashMap<>();

    public static void clearCache()
    {
        cachedAnimations.clear();
    }

    public static KeyframeAnimation loadFromResource(ResourceLocation location) throws IOException
    {
        KeyframeAnimation cached = cachedAnimations.get(location);
        if (cached != null)
        {
            return cached;
        }

        try (InputStream stream = PackTrust.open(location))
        {
            KeyframeAnimation animation = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), KeyframeAnimation.class);
            if (animation != null)
            {
                cachedAnimations.put(location, animation);
            }
            return animation;
        }
    }

    /** Loads the clip at a resource key ({@code modid:path}); null for a key without a namespace. */
    public static KeyframeAnimation loadFromPath(String key) throws IOException
    {
        int colonIndex = key.indexOf(':');
        if (colonIndex == -1)
        {
            return null;
        }
        return loadFromResource(new ResourceLocation(key.substring(0, colonIndex), key.substring(colonIndex + 1)));
    }

}
