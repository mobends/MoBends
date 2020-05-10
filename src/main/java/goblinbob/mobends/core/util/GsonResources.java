package goblinbob.mobends.core.util;

import goblinbob.mobends.core.kumo.KumoSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class GsonResources
{

    private static Map<ResourceLocation, Object> cache = new HashMap<>();

    public static <T> T get(ResourceLocation location, Class<T> classOfT) throws IOException
    {
        final InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();

        if (cache.containsKey(location))
        {
            //noinspection unchecked
            return (T) cache.get(location);
        }
        else
        {
            T animation = KumoSerializer.INSTANCE.gson.fromJson(new InputStreamReader(stream), classOfT);
            cache.put(location, animation);
            return animation;
        }
    }

}
