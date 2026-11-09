package goblinbob.mobends.core.connection;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.supporters.AccessorySettings;
import goblinbob.mobends.core.supporters.SupporterContent;
import goblinbob.mobends.core.util.ConnectionHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fetches players' accessory settings ({@code GET /api/accessory}), one at a time, on a thread of
 * its own, and hands them to {@link SupporterContent} on the render thread. A player whose fetch
 * failed (no connection, an error status, a response that isn't the settings) isn't fetched again
 * for a while: the renderer asks for the settings of a player every frame until they arrive.
 * <p>
 * Its bookkeeping is the render thread's alone; the requests are the download thread's.
 */
final class PlayerSettingsDownloader {
    private static final long RETRY_DELAY_MS = 30000;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "Mo' Bends player settings");
        thread.setDaemon(true);
        return thread;
    });
    /** The players being fetched, by name. */
    private final Set<String> loading = new HashSet<>();
    /** When each player whose fetch failed may be fetched again, in {@link System#currentTimeMillis} time. */
    private final Map<String, Long> retryTimes = new HashMap<>();

    /** Render thread. */
    void fetchSettingsForPlayer(String playerName, @Nullable UUID uuid) {
        Long retryTime = retryTimes.get(playerName);
        if (retryTime != null && System.currentTimeMillis() < retryTime) {
            return;
        }
        if (!loading.add(playerName)) {
            return;
        }
        executor.execute(() -> {
            Map<String, AccessorySettings> settings = download(playerName, uuid);
            ConnectionHelper.onRenderThread(() -> {
                loading.remove(playerName);
                if (settings == null) {
                    retryTimes.put(playerName, System.currentTimeMillis() + RETRY_DELAY_MS);
                } else {
                    retryTimes.remove(playerName);
                    SupporterContent.registerPlayerAccessorySettings(playerName, settings);
                }
            });
        });
    }

    /** Download thread. The settings, or null if they couldn't be fetched. */
    @Nullable
    private static Map<String, AccessorySettings> download(String playerName, @Nullable UUID uuid) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("username", playerName);
        // The profile's UUID finds an account linked through the mod even after the player renamed it.
        if (uuid != null) {
            params.put("uuid", uuid.toString());
        }
        try {
            Response response = ConnectionHelper.get("/api/accessory", params, Response.class);
            if (response == null || response.settings == null) {
                MoBends.LOGGER.warn("The Mo' Bends API's settings for {} have no settings", playerName);
                return null;
            }
            MoBends.LOGGER.info("Fetched the accessory settings of {}: {}", playerName, response.settings.keySet());
            return response.settings;
        } catch (IOException e) {
            if (!ConnectionHelper.isOffline(e)) {
                MoBends.LOGGER.warn("Couldn't fetch the accessory settings of {}: {}", playerName, e.toString());
            }
            return null;
        }
    }

    private static final class Response {
        Map<String, AccessorySettings> settings;
    }
}
