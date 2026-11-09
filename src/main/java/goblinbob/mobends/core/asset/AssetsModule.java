package goblinbob.mobends.core.asset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.util.ConnectionHelper;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The downloadable assets: the supporter accessories' models and textures (1.12's
 * {@code AssetsModule}). They're kept in {@code config/mobends/assets/}, with the manifest they came
 * with in {@code config/mobends/asset_manifest.json}. As the game starts, the API's manifest is
 * fetched in the background, and what's new or newer is downloaded; then the textures and models
 * read so far are dropped on the render thread, to be read again from the new files.
 * <p>
 * The textures and models are read from disk as they're first drawn ({@link AssetTextures},
 * {@link AssetModels}). Neither goes through the resource manager, so a resource reload leaves
 * them alone (1.12 had to register them again on every reload).
 */
public final class AssetsModule {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Nullable
    private static Path assetsDirectory;
    @Nullable
    private static Path manifestFile;
    /** Read anywhere, replaced by the download thread. */
    @Nullable
    private static volatile AssetManifest localManifest;
    private static final AtomicBoolean updating = new AtomicBoolean();

    private AssetsModule() {
    }

    /** Reads the manifest of the assets on disk. Once, at start-up. */
    public static void init(Path configDir) {
        Path directory = configDir.resolve(MoBends.MOD_ID);
        assetsDirectory = directory.resolve("assets");
        manifestFile = directory.resolve("asset_manifest.json");
        if (Files.isRegularFile(manifestFile)) {
            try (Reader reader = Files.newBufferedReader(manifestFile, StandardCharsets.UTF_8)) {
                localManifest = GSON.fromJson(reader, AssetManifest.class);
            } catch (IOException | JsonParseException e) {
                MoBends.LOGGER.warn("Couldn't read {}: {}", manifestFile, e.toString());
            }
        }
    }

    /**
     * Fetches the API's manifest in the background and downloads what's new; if anything was, the
     * textures and models are read again, on the render thread. Does nothing while an update is
     * under way.
     */
    public static void updateInBackground() {
        if (assetsDirectory == null || !updating.compareAndSet(false, true)) {
            return;
        }
        ConnectionHelper.runInBackground("asset download", () -> {
            try {
                if (updateAssets()) {
                    ConnectionHelper.onRenderThread(AssetsModule::onAssetsChanged);
                }
            } finally {
                updating.set(false);
            }
        });
    }

    /** Download thread. Whether new assets were downloaded. */
    private static boolean updateAssets() {
        AssetManifest online;
        try {
            online = ConnectionHelper.get("/api/asset/manifest", Map.of(), AssetManifest.class);
        } catch (IOException e) {
            if (ConnectionHelper.isOffline(e)) {
                MoBends.LOGGER.info("Couldn't reach the Mo' Bends API for the asset manifest ({})", e.toString());
            } else {
                MoBends.LOGGER.warn("Couldn't fetch the asset manifest: {}", e.toString());
            }
            return false;
        }
        if (online == null) {
            return false;
        }
        List<AssetManifest.AssetDefinition> toUpdate = AssetManifest.getAssetsToUpdate(localManifest, online);
        MoBends.LOGGER.info("Fetched the asset manifest: {} assets, {} to download", online.assets().size(), toUpdate.size());
        if (toUpdate.isEmpty()) {
            return false;
        }
        // Assets come from outside the game: web pages only.
        if (!online.baseUrl().startsWith("https://") && !online.baseUrl().startsWith("http://localhost")) {
            MoBends.LOGGER.warn("The asset manifest's base URL isn't https: {}", online.baseUrl());
            return false;
        }
        try {
            for (AssetManifest.AssetDefinition asset : toUpdate) {
                download(online, asset);
            }
        } catch (IOException e) {
            // The manifest isn't kept, so what failed is downloaded again next time.
            MoBends.LOGGER.warn("Couldn't download the assets: {}", e.toString());
            return false;
        }
        storeManifest(online);
        return true;
    }

    private static void download(AssetManifest manifest, AssetManifest.AssetDefinition asset) throws IOException {
        Path file = fileOf(asset.path());
        byte[] data = ConnectionHelper.download(URI.create(manifest.baseUrl() + asset.path()));
        Files.createDirectories(file.getParent());
        // Written aside and moved over, so a draw never reads half a file.
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(temporary, data);
        move(temporary, file);
        MoBends.LOGGER.info("Downloaded the asset {} (version {})", asset.path(), asset.version());
    }

    private static void storeManifest(AssetManifest manifest) {
        localManifest = manifest;
        try {
            Files.createDirectories(manifestFile.getParent());
            Path temporary = manifestFile.resolveSibling(manifestFile.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(manifest, writer);
            }
            move(temporary, manifestFile);
        } catch (IOException e) {
            MoBends.LOGGER.warn("Couldn't save {}: {}", manifestFile, e.toString());
        }
    }

    private static void move(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Render thread: the files changed, so what was read of them is read again. */
    private static void onAssetsChanged() {
        AssetTextures.clearCache();
        AssetModels.clearCache();
    }

    /**
     * The file of the asset at {@code path}. The paths come from the API, so one that isn't a
     * plain relative path ({@code models/x.json}), or would lead out of the assets folder, is refused.
     */
    public static Path fileOf(String path) throws IOException {
        if (assetsDirectory == null) {
            throw new IOException("The assets folder isn't set up");
        }
        if (ResourceLocation.tryBuild(MoBends.MOD_ID, path) == null) {
            throw new IOException("Not an asset path: " + path);
        }
        Path root = assetsDirectory.toAbsolutePath().normalize();
        Path file = root.resolve(path).normalize();
        if (!file.startsWith(root) || file.equals(root)) {
            throw new IOException("The asset path leads out of the assets folder: " + path);
        }
        return file;
    }
}
