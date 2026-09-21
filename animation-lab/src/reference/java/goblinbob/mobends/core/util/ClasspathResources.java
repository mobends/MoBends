package goblinbob.mobends.core.util;

import net.minecraft.util.ResourceLocation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/** LAB ONLY. Resolves "domain:path" the way Minecraft does: assets/domain/path on the classpath. */
public class ClasspathResources
{
    public static InputStream open(ResourceLocation location) throws IOException
    {
        final String path = "assets/" + location.getResourceDomain() + "/" + location.getResourcePath();
        InputStream stream = ClasspathResources.class.getClassLoader().getResourceAsStream(path);
        if (stream == null)
        {
            throw new FileNotFoundException("Resource not on the lab classpath: " + path);
        }
        return stream;
    }
}
