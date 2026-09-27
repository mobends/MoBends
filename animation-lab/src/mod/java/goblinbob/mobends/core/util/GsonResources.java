package goblinbob.mobends.core.util;

import goblinbob.mobends.core.kumo.KumoSerializer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/** LAB SHIM. Loads JSON resources from the classpath instead of Minecraft's resource manager. */
public class GsonResources
{
    private static final Map<ResourceLocation, Object> cache = new HashMap<>();

    public static void clearCache()
    {
        cache.clear();
    }

    public static <T> T get(ResourceLocation location, Class<T> classOfT) throws IOException
    {
        if (cache.containsKey(location))
        {
            //noinspection unchecked
            return (T) cache.get(location);
        }

        try (InputStream stream = ClasspathResources.open(location))
        {
            T resource = KumoSerializer.INSTANCE.gson.fromJson(new InputStreamReader(stream), classOfT);
            cache.put(location, resource);
            return resource;
        }
    }
}
