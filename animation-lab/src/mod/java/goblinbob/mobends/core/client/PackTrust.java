package goblinbob.mobends.core.client;

import goblinbob.mobends.core.util.ClasspathResources;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;

/** LAB SHIM. The lab has no resource packs: everything is trusted and read from the classpath. */
public final class PackTrust
{

    private PackTrust()
    {
    }

    public static boolean isTrusted(ResourceLocation location)
    {
        return true;
    }

    public static InputStream open(ResourceLocation location) throws IOException
    {
        return ClasspathResources.open(location);
    }

}
