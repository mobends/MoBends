package goblinbob.mobends.core.client;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.network.ResourcePackPolicy;
import goblinbob.mobends.core.pack.PackSignature;
import goblinbob.mobends.core.pack.PackSigningKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

/**
 * Tells trusted animation content from untrusted: the resource packs the player enabled are
 * untrusted (anyone can install one); the mod, other mods, vanilla and the server's own resource
 * pack are trusted. A resource is untrusted when an enabled resource pack supplies it, because that
 * version is the one the game loads, unless that pack is one of Mo' Bends' own: its
 * {@link PackSignature} verifies with a key the mod ships and lists the resource with exactly the
 * bytes the pack has (the Legacy Mo' Bends pack). Those bytes are what {@link #open} then returns, so
 * what is checked is what is read.
 */
public final class PackTrust
{

    private static final ResourceLocation SIGNATURE = new ResourceLocation(PackSignature.LOCATION);

    /** Each user pack's verified signature, empty for a pack without a genuine one; until a reload. */
    private static final Map<IResourcePack, Optional<PackSignature>> signatures = new IdentityHashMap<>();
    /** Each resource's {@link #signedContent}; until a reload. */
    private static final Map<ResourceLocation, Optional<byte[]>> signedContent = new HashMap<>();

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

    /** Forgets the packs' signatures and what they cover: on every reload of the animation. */
    public static synchronized void clearCache()
    {
        signatures.clear();
        signedContent.clear();
    }

    public static boolean isTrusted(ResourceLocation location)
    {
        for (IResourcePack pack : userPacks())
        {
            if (pack.resourceExists(location)) return signedContent(location) != null;
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
        byte[] signed = signedContent(location);
        if (signed != null)
        {
            return new ByteArrayInputStream(signed);
        }
        if (!trustedOnly(location) || isTrusted(location))
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

    /** Whether {@link #open} skips the resource packs' versions of {@code location}, as the server's policy says. */
    private static boolean trustedOnly(ResourceLocation location)
    {
        ResourcePackPolicy policy = AnimationPolicy.INSTANCE.current();
        return policy == ResourcePackPolicy.DENY || (policy == ResourcePackPolicy.LIMITED && isGeometry(location));
    }

    /** Whether the version of {@code location} that {@link #open} reads is trusted. */
    public static boolean opensTrusted(ResourceLocation location)
    {
        return trustedOnly(location) || isTrusted(location);
    }

    /**
     * The bytes of the version of {@code location} the game loads, when a user pack supplies it and
     * that pack's genuine signature covers them; null otherwise (no user pack supplies it, the pack
     * isn't signed, or the file isn't the one it signed). The server's pack, above the user packs,
     * may replace it: then it is the server's version that loads, and this is null.
     */
    @Nullable
    private static synchronized byte[] signedContent(ResourceLocation location)
    {
        return signedContent.computeIfAbsent(location, PackTrust::readSignedContent).orElse(null);
    }

    private static Optional<byte[]> readSignedContent(ResourceLocation location)
    {
        IResourcePack serverPack = Minecraft.getMinecraft().getResourcePackRepository().getServerResourcePack();
        IResourcePack top = null;
        for (IResourcePack pack : userPacks()) // lowest priority first
        {
            if (pack.resourceExists(location)) top = pack;
        }
        if (top == null || (serverPack != null && serverPack.resourceExists(location)))
        {
            return Optional.empty();
        }
        Optional<PackSignature> signature = signatures.computeIfAbsent(top, PackTrust::readSignature);
        if (!signature.isPresent())
        {
            return Optional.empty();
        }
        try (InputStream stream = top.getInputStream(location))
        {
            byte[] bytes = IOUtils.toByteArray(stream);
            return signature.get().covers(location.toString(), bytes) ? Optional.of(bytes) : Optional.empty();
        }
        catch (IOException e)
        {
            Core.LOG.log(Level.WARNING, "Could not read " + location + " from " + top.getPackName(), e);
            return Optional.empty();
        }
    }

    /** The pack's signature, when it has one and one of the mod's keys made it. */
    private static Optional<PackSignature> readSignature(IResourcePack pack)
    {
        if (pack.resourceExists(SIGNATURE))
        {
            try (InputStream stream = pack.getInputStream(SIGNATURE))
            {
                PackSignature read = PackSignature.read(new InputStreamReader(stream, StandardCharsets.UTF_8));
                if (read.verify(PackSigningKeys.trusted()))
                {
                    Core.LOG.info(pack.getPackName() + " is signed by Mo' Bends; its " + read.files().size() + " signed files are trusted");
                    return Optional.of(read);
                }
                Core.LOG.warning(pack.getPackName() + " has a pack signature Mo' Bends didn't make; its animation is untrusted");
            }
            catch (IOException e)
            {
                Core.LOG.log(Level.WARNING, pack.getPackName() + " has a pack signature that can't be read; its animation is untrusted", e);
            }
        }
        return Optional.empty();
    }

    /** Model definitions: they decide a model's geometry. */
    public static boolean isGeometry(ResourceLocation location)
    {
        return location.getResourcePath().startsWith("bends/models/");
    }

}
