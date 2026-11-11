package goblinbob.mobends.core.asset;

import com.mojang.blaze3d.platform.NativeImage;
import goblinbob.mobends.MoBends;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The textures of the downloaded assets (1.12's {@code AssetTexture}): each read from its file the
 * first time it's drawn, and registered with the texture manager as {@code mobends:assets/<path>}.
 * A texture that can't be read isn't tried again until the assets change. Render thread only.
 */
public final class AssetTextures {
    private static final Map<String, Optional<ResourceLocation>> TEXTURES = new HashMap<>();

    private AssetTextures() {
    }

    /** The texture of the asset at {@code path} ({@code textures/x.png}), or null if it can't be read. */
    @Nullable
    public static ResourceLocation get(String path) {
        return TEXTURES.computeIfAbsent(path, AssetTextures::load).orElse(null);
    }

    private static Optional<ResourceLocation> load(String path) {
        ResourceLocation id = ResourceLocation.tryBuild(MoBends.MOD_ID, "assets/" + path);
        if (id == null) {
            MoBends.LOGGER.warn("Not an asset texture: {}", path);
            return Optional.empty();
        }
        try (InputStream in = Files.newInputStream(AssetsModule.fileOf(path))) {
            NativeImage image = NativeImage.read(in);
            //? if >=26.1 {
            /*Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, image));
            *///?} else {
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(image));
            //?}
            return Optional.of(id);
        } catch (IOException e) {
            MoBends.LOGGER.warn("Couldn't read the asset texture {}: {}", path, e.toString());
            return Optional.empty();
        }
    }

    /** Releases every texture read so far, to be read again as they're drawn. */
    static void clearCache() {
        for (Optional<ResourceLocation> id : TEXTURES.values()) {
            id.ifPresent(Minecraft.getInstance().getTextureManager()::release);
        }
        TEXTURES.clear();
    }
}
