package goblinbob.mobends.core.client;

import goblinbob.mobends.core.network.ResourcePackPolicy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tells trusted animation content from untrusted: the resource packs the player enabled are
 * untrusted (anyone can install one); the mod, other mods, vanilla and the server's own resource
 * pack are trusted. A resource is untrusted when an enabled resource pack supplies it, because that
 * version is the one the game loads.
 */
public final class PackTrust
{

    private PackTrust()
    {
    }

    /** The resource packs the player enabled, lowest priority first (not the server's, which is trusted). */
    public static List<IResourcePack> userPacks()
    {
        List<IResourcePack> packs = new ArrayList<>();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.getResourcePackRepository() == null)
        {
            return packs;
        }
        for (ResourcePackRepository.Entry entry : mc.getResourcePackRepository().getRepositoryEntries())
        {
            packs.add(entry.getResourcePack());
        }
        return packs;
    }

    public static boolean isUserPack(IResourcePack pack)
    {
        for (IResourcePack userPack : userPacks())
        {
            if (userPack == pack) return true;
        }
        return false;
    }

    public static boolean isTrusted(ResourceLocation location)
    {
        for (IResourcePack pack : userPacks())
        {
            if (pack.resourceExists(location)) return false;
        }
        return true;
    }

    /**
     * Opens a resource for animation. A version a resource pack supplies is skipped for the highest
     * trusted one when the server denies resource packs' animation, and, for model definitions
     * (geometry, {@code bends/models/}), when it limits it.
     */
    public static InputStream open(ResourceLocation location) throws IOException
    {
        ResourcePackPolicy policy = AnimationPolicy.INSTANCE.current();
        boolean trustedOnly = policy == ResourcePackPolicy.DENY || (policy == ResourcePackPolicy.LIMITED && isGeometry(location));
        if (!trustedOnly || isTrusted(location))
        {
            return Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
        }
        Set<String> untrusted = new HashSet<>();
        for (IResourcePack pack : userPacks())
        {
            untrusted.add(pack.getPackName());
        }
        // Lowest priority first: the last trusted one is the one the game would load without the packs.
        List<IResource> resources = Minecraft.getMinecraft().getResourceManager().getAllResources(location);
        IResource trusted = null;
        for (IResource resource : resources)
        {
            if (!untrusted.contains(resource.getResourcePackName())) trusted = resource;
        }
        for (IResource resource : resources)
        {
            if (resource != trusted) IOUtils.closeQuietly(resource);
        }
        if (trusted == null)
        {
            throw new FileNotFoundException(location + " (only a resource pack supplies it, and the server doesn't allow that)");
        }
        return trusted.getInputStream();
    }

    /** Model definitions: they decide a model's geometry. */
    public static boolean isGeometry(ResourceLocation location)
    {
        return location.getResourcePath().startsWith("bends/models/");
    }

}
