package goblinbob.mobends.core.client;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.network.ResourcePackPolicy;
import goblinbob.mobends.core.pack.PackSignature;
import goblinbob.mobends.core.pack.PackSigningKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Tells trusted animation content from untrusted: the resource packs the player enabled are
 * untrusted (anyone can install one); the mod, other mods, vanilla and the server's own resource
 * pack are trusted. A resource is untrusted when one of the player's packs supplies the version the
 * game loads, unless that pack is one of Mo' Bends' own: its {@link PackSignature} verifies with a key
 * the mod ships and lists the resource with exactly the bytes the pack has (the Legacy Mo' Bends
 * pack). Those bytes are what {@link #open} then returns, so what is checked is what is read.
 * <p>
 * The player's packs are those from the {@code resourcepacks} folder, whose ids start with
 * {@code file/} on every loader (mods' packs, vanilla's and the server's have other ids).
 */
public final class PackTrust {
    private static final ResourceLocation SIGNATURE = ResourceLocation.parse(PackSignature.LOCATION);
    private static final String USER_PACK_PREFIX = "file/";

    /** Each user pack's verified signature, empty for a pack without a genuine one; until a reload. */
    private static final Map<String, Optional<PackSignature>> signatures = new HashMap<>();
    /** Each resource's {@link #signedContent}; until a reload. */
    private static final Map<ResourceLocation, Optional<byte[]>> signedContent = new HashMap<>();

    private PackTrust() {
    }

    /** Whether the pack with this id is one the player enabled from the {@code resourcepacks} folder. */
    public static boolean isUserPack(String packId) {
        return packId.startsWith(USER_PACK_PREFIX);
    }

    /** Forgets the packs' signatures and what they cover: on every reload of the animation. */
    public static synchronized void clearCache() {
        signatures.clear();
        signedContent.clear();
    }

    private static ResourceManager resources() {
        return Minecraft.getInstance().getResourceManager();
    }

    /** Whether the version of {@code location} the game loads is trusted: no user pack's, or one Mo' Bends signed. */
    public static boolean isTrusted(ResourceLocation location) {
        Optional<Resource> top = resources().getResource(location);
        return top.isEmpty() || !isUserPack(top.get().sourcePackId()) || signedContent(location) != null;
    }

    /**
     * Opens a resource for animation. A version a resource pack supplies is skipped for the highest
     * trusted one when the server denies resource packs' animation, and, for model definitions
     * (geometry, {@code bends/models/}), when it limits it.
     */
    public static InputStream open(ResourceLocation location) throws IOException {
        byte[] signed = signedContent(location);
        if (signed != null) {
            return new ByteArrayInputStream(signed);
        }
        if (!trustedOnly(location) || isTrusted(location)) {
            return resources().open(location);
        }
        // Lowest priority first: the last trusted one is the one the game would load without the packs.
        Resource trusted = null;
        for (Resource resource : resources().getResourceStack(location)) {
            if (!isUserPack(resource.sourcePackId())) {
                trusted = resource;
            }
        }
        if (trusted == null) {
            throw new FileNotFoundException(location + " (only a resource pack supplies it, and the server doesn't allow that)");
        }
        return trusted.open();
    }

    /** Whether {@link #open} skips the resource packs' versions of {@code location}, as the server's policy says. */
    private static boolean trustedOnly(ResourceLocation location) {
        ResourcePackPolicy policy = AnimationPolicy.INSTANCE.current();
        return policy == ResourcePackPolicy.DENY || (policy == ResourcePackPolicy.LIMITED && isGeometry(location));
    }

    /** Whether the version of {@code location} that {@link #open} reads is trusted. */
    public static boolean opensTrusted(ResourceLocation location) {
        return trustedOnly(location) || isTrusted(location);
    }

    /**
     * The bytes of the version of {@code location} the game loads, when a user pack supplies it and
     * that pack's genuine signature covers them; null otherwise (no user pack supplies it, the pack
     * isn't signed, or the file isn't the one it signed).
     */
    @Nullable
    private static synchronized byte[] signedContent(ResourceLocation location) {
        return signedContent.computeIfAbsent(location, PackTrust::readSignedContent).orElse(null);
    }

    private static Optional<byte[]> readSignedContent(ResourceLocation location) {
        Optional<Resource> top = resources().getResource(location);
        if (top.isEmpty() || !isUserPack(top.get().sourcePackId())) {
            return Optional.empty();
        }
        String pack = top.get().sourcePackId();
        Optional<PackSignature> signature = signatures.computeIfAbsent(pack, PackTrust::readSignature);
        if (signature.isEmpty()) {
            return Optional.empty();
        }
        try (InputStream stream = top.get().open()) {
            byte[] bytes = stream.readAllBytes();
            return signature.get().covers(location.toString(), bytes) ? Optional.of(bytes) : Optional.empty();
        } catch (IOException e) {
            MoBends.LOGGER.warn("Could not read {} from {}", location, pack, e);
            return Optional.empty();
        }
    }

    /** The pack's signature, when it has one and one of the mod's keys made it. */
    private static Optional<PackSignature> readSignature(String pack) {
        List<Resource> stack = resources().getResourceStack(SIGNATURE);
        for (Resource resource : stack) {
            if (!pack.equals(resource.sourcePackId())) {
                continue;
            }
            try (InputStream stream = resource.open()) {
                PackSignature read = PackSignature.read(new InputStreamReader(stream, StandardCharsets.UTF_8));
                if (read.verify(PackSigningKeys.trusted())) {
                    MoBends.LOGGER.info("{} is signed by Mo' Bends; its {} signed files are trusted", pack, read.files().size());
                    return Optional.of(read);
                }
                MoBends.LOGGER.warn("{} has a pack signature Mo' Bends didn't make; its animation is untrusted", pack);
            } catch (IOException e) {
                MoBends.LOGGER.warn("{} has a pack signature that can't be read; its animation is untrusted", pack, e);
            }
        }
        return Optional.empty();
    }

    /** Model definitions: they decide a model's geometry. */
    public static boolean isGeometry(ResourceLocation location) {
        return location.getPath().startsWith("bends/models/");
    }
}
