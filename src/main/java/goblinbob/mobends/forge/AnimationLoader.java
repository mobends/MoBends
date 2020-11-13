package goblinbob.mobends.forge;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.serial.StreamSerialInput;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AnimationLoader
{

    /**
     * This could hold animations that are embedded in kumo animators.
     */
    private static Map<String, KeyframeAnimation> internalRegistry = new HashMap<>();

    /**
     * This holds animations that are loaded from mod resources.
     */
    private static Map<ResourceLocation, KeyframeAnimation> cachedAnimations = new HashMap<>();

    public static void clearCache()
    {
        internalRegistry.clear();
        cachedAnimations.clear();
    }

    public static KeyframeAnimation loadFromFile(File file) throws IOException
    {
        StreamSerialInput in = new StreamSerialInput(new BufferedInputStream(new FileInputStream(file)));
        return KeyframeAnimation.deserialize(in);
    }

    public static KeyframeAnimation loadFromResource(ResourceLocation location) throws IOException
    {
        InputStream stream = Minecraft.getInstance().getResourceManager().getResource(location).getInputStream();
        StreamSerialInput in = new StreamSerialInput(stream);

        if (cachedAnimations.containsKey(location))
        {
            return cachedAnimations.get(location);
        }
        else
        {
            KeyframeAnimation animation = KeyframeAnimation.deserialize(in);

            if (animation != null)
            {
                cachedAnimations.put(location, animation);
            }
            return animation;
        }
    }

    /**
     * Loads the animation from an ambiguous path, either from a resource (modid:path) or an internal registry (key).
     * @param key
     * @return
     */
    public static KeyframeAnimation loadFromPath(String key) throws IOException
    {
        int colonIndex = key.indexOf(":");
        if (colonIndex != -1)
        {
            // The passed path is a resource path.
            final String domain = key.substring(0, colonIndex);
            final String path = key.substring(colonIndex + 1);

            return loadFromResource(new ResourceLocation(domain, path));
        }

        return internalRegistry.get(key);
    }

}
