package goblinbob.mobends.core.client;

import net.minecraft.util.ResourceLocation;

/** LAB SHIM. The lab has no resource packs: everything is trusted. */
public final class PackTrust
{

    private PackTrust()
    {
    }

    public static boolean isTrusted(ResourceLocation location)
    {
        return true;
    }

}
