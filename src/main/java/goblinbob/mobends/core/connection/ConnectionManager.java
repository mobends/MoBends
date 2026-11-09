package goblinbob.mobends.core.connection;

import com.google.gson.JsonObject;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.asset.AssetsModule;
import goblinbob.mobends.core.supporters.SupporterContent;
import goblinbob.mobends.core.util.ApiErrorException;
import goblinbob.mobends.core.util.ConnectionHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The game's activity on the mobends.com API (1.12's {@code ConnectionManager} and {@code PingTask}):
 * it joins once as the game starts, then pings as often as the join's response asks, on a thread
 * of its own. Both carry the mod's version and the checksum of its jar, which tells the API an
 * official release from a fork.
 * <p>
 * Players' accessory settings are fetched only once the API has answered ({@link #fetchSettingsForPlayer}):
 * if it can't be reached, there's nothing to fetch. A join the API refuses (it answers, with an
 * error) doesn't stop them.
 */
public final class ConnectionManager {
    private static final String DEV_CHECKSUM = "dev";
    /** The shortest ping interval taken from the API, in seconds, whatever it says. */
    private static final float MIN_PING_INTERVAL = 10.0F;

    private static final ScheduledExecutorService ACTIVITY = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "Mo' Bends API activity");
        thread.setDaemon(true);
        return thread;
    });
    private static final PlayerSettingsDownloader SETTINGS = new PlayerSettingsDownloader();

    @Nullable
    private static Path modFile;
    /** The jar's checksum, worked out on the activity thread before the join; null if it couldn't be. */
    @Nullable
    private static String checksum;
    /** Set on the activity thread once the join succeeded; read anywhere. */
    private static volatile boolean joined;
    /** Set on the activity thread once the API answered the join, whatever it said; read anywhere. */
    private static volatile boolean reachable;

    private ConnectionManager() {
    }

    /**
     * Joins the API in the background, then starts pinging it. Once, at start-up.
     *
     * @param modFile the jar the mod was loaded from, a directory in development, or null if unknown
     */
    public static void init(@Nullable Path modFile) {
        ConnectionManager.modFile = modFile;
        ACTIVITY.execute(() -> {
            checksum = computeChecksum(ConnectionManager.modFile);
            join();
        });
    }

    /**
     * What 1.12's refresh did to the API's modules: joins again if the join failed, fetches the
     * accessory catalogue and the assets anew, and forgets every player's settings, which are then
     * fetched again as the players are drawn. For the Mo' Bends menu's refresh.
     */
    public static void refresh() {
        if (!joined) {
            ACTIVITY.execute(ConnectionManager::join);
        }
        SupporterContent.refresh();
        AssetsModule.updateInBackground();
    }

    /** Whether the game has joined the API. */
    public static boolean isJoined() {
        return joined;
    }

    /** Whether the API answered the join, whatever it said: players' settings can be fetched. */
    public static boolean isReachable() {
        return reachable;
    }

    /**
     * Fetches the accessory settings of the player named {@code playerName} in the background, if
     * the API answered the join ({@link #isReachable}); they're handed to {@link SupporterContent} on the render thread.
     * Render thread only.
     *
     * @param uuid the player's profile UUID, or null if it isn't known
     */
    public static void fetchSettingsForPlayer(String playerName, @Nullable UUID uuid) {
        if (reachable) {
            SETTINGS.fetchSettingsForPlayer(playerName, uuid);
        }
    }

    /** Activity thread. */
    private static void join() {
        if (joined) {
            return;
        }
        JoinResponse response;
        try {
            response = ConnectionHelper.post("/api/activity/join", createActivityBody(), JoinResponse.class);
        } catch (ApiErrorException e) {
            reachable = true;
            MoBends.LOGGER.warn("The Mo' Bends API refused the join: {}. Contact the developers if this keeps happening.", e.getMessage());
            return;
        } catch (IOException e) {
            if (ConnectionHelper.isOffline(e)) {
                MoBends.LOGGER.info("Couldn't reach the Mo' Bends API ({}); supporter accessories are off.", e.toString());
            } else {
                MoBends.LOGGER.warn("Couldn't join the Mo' Bends API; supporter accessories are off. Contact the developers if this "
                    + "keeps happening.", e);
            }
            return;
        }
        reachable = true;
        float interval = Math.max(MIN_PING_INTERVAL, response == null ? 0.0F : response.pingInterval);
        MoBends.LOGGER.info("Joined the Mo' Bends API at {}; pinging every {} s", ConnectionHelper.endpoint(""), interval);
        joined = true;
        // As 1.12's PingTask: the first ping right away, then one every interval.
        long intervalMs = (long) (interval * 1000);
        ACTIVITY.scheduleWithFixedDelay(ConnectionManager::ping, 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    /** Activity thread. */
    private static void ping() {
        try {
            ConnectionHelper.post("/api/activity/ping", createActivityBody(), Void.class);
        } catch (IOException e) {
            if (!ConnectionHelper.isOffline(e)) {
                MoBends.LOGGER.warn("The Mo' Bends API ping failed: {}", e.toString());
            }
        }
    }

    /**
     * The body shared by the join and ping requests. The version is said as 1.12's
     * {@code ModStatics.VERSION_STRING} said it, {@code <mod>/<minecraft>-<loader>}
     * ({@code 2.0.0-alpha.1/1.21.1-fabric}), so the API can tell the builds apart.
     */
    private static JsonObject createActivityBody() {
        JsonObject body = new JsonObject();
        body.addProperty("app", "mobends");
        body.addProperty("version", MoBends.VERSION + "/" + MoBends.MINECRAFT + "-" + MoBends.platform().loaderName());
        body.addProperty("checksum", checksum);
        return body;
    }

    /**
     * SHA-256 of the jar, as lowercase hex; {@link #DEV_CHECKSUM} in development, where the mod is
     * loaded from a directory; null if it can't be worked out.
     */
    @Nullable
    private static String computeChecksum(@Nullable Path source) {
        if (source == null) {
            return null;
        }
        if (Files.isDirectory(source)) {
            return DEV_CHECKSUM;
        }
        try (InputStream in = new DigestInputStream(Files.newInputStream(source), MessageDigest.getInstance("SHA-256"))) {
            byte[] buffer = new byte[8192];
            while (in.read(buffer) >= 0) {
                // Read through, for the digest.
            }
            return HexFormat.of().formatHex(((DigestInputStream) in).getMessageDigest().digest());
        } catch (IOException | NoSuchAlgorithmException | UnsupportedOperationException e) {
            MoBends.LOGGER.warn("Couldn't compute the mod's checksum: {}", e.toString());
            return null;
        }
    }

    private static final class JoinResponse {
        /** The delay between pings, in seconds. */
        float pingInterval;
    }
}
