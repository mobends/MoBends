package goblinbob.mobends.core.animation.keyframe;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import goblinbob.mobends.core.util.ClasspathResources;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * LAB SHIM. Same API as the mod's AnimationLoader, but resources are read from the classpath.
 */
public class AnimationLoader
{
    private static Map<String, KeyframeAnimation> internalRegistry = new HashMap<>();
    private static Map<ResourceLocation, KeyframeAnimation> cachedAnimations = new HashMap<>();

    public static void clearCache()
    {
        internalRegistry.clear();
        cachedAnimations.clear();
    }

    public static KeyframeAnimation loadFromFile(File file) throws IOException
    {
        if (file.getName().endsWith(".json"))
        {
            JsonReader fileReader = new JsonReader(new FileReader(file));
            return (new Gson()).fromJson(fileReader, KeyframeAnimation.class);
        }
        else
        {
            return BinaryAnimationLoader.loadFromBinaryInputStream(new BufferedInputStream(new FileInputStream(file)));
        }
    }

    public static KeyframeAnimation loadFromString(String animationJson)
    {
        return (new Gson()).fromJson(animationJson, KeyframeAnimation.class);
    }

    public static KeyframeAnimation loadFromResource(ResourceLocation location) throws IOException
    {
        if (cachedAnimations.containsKey(location))
        {
            return cachedAnimations.get(location);
        }

        try (InputStream stream = ClasspathResources.open(location))
        {
            KeyframeAnimation animation;
            if (location.getResourcePath().endsWith(".json"))
            {
                animation = (new Gson()).fromJson(new InputStreamReader(stream), KeyframeAnimation.class);
            }
            else
            {
                animation = BinaryAnimationLoader.loadFromBinaryInputStream(stream);
            }

            if (animation != null)
            {
                cachedAnimations.put(location, animation);
            }
            return animation;
        }
    }

    public static KeyframeAnimation loadFromPath(String key) throws IOException
    {
        int colonIndex = key.indexOf(":");
        if (colonIndex != -1)
        {
            final String domain = key.substring(0, colonIndex);
            final String path = key.substring(colonIndex + 1);
            return loadFromResource(new ResourceLocation(domain, path));
        }

        return internalRegistry.get(key);
    }
}
