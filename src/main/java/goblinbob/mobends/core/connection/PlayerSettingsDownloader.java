package goblinbob.mobends.core.connection;

import com.google.gson.JsonParseException;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.supporters.SupporterContent;
import goblinbob.mobends.core.util.ConnectionHelper;
import org.apache.http.conn.HttpHostConnectException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Fetches players' accessory settings, one at a time. A player whose fetch failed (no connection, an error status, or
 * a response that isn't the settings) isn't fetched again for a while: the renderer asks for the settings of a player
 * every frame until they arrive.
 */
public class PlayerSettingsDownloader implements Runnable
{
    private static final long RETRY_DELAY_MS = 30000;

    private final String apiUrl;
    private final long retryDelayMs;
    /** The players being fetched. It guards {@link #retryTimes} too. */
    private final Set<String> loadingPlayersSet = new HashSet<>();
    /** When each player whose fetch failed may be fetched again, in {@link System#currentTimeMillis} time. */
    private final Map<String, Long> retryTimes = new HashMap<>();
    private BlockingQueue<PlayerSettingsTask> playerSettingsTasks = new LinkedBlockingQueue<>();

    public PlayerSettingsDownloader(String apiUrl)
    {
        this(apiUrl, RETRY_DELAY_MS);
    }

    public PlayerSettingsDownloader(String apiUrl, long retryDelayMs)
    {
        this.apiUrl = apiUrl;
        this.retryDelayMs = retryDelayMs;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                // Get the next task or wait for one to appear.
                PlayerSettingsTask task = playerSettingsTasks.take();
                boolean failed = true;

                try
                {
                    performTask(task);
                    failed = false;
                }
                catch (HttpHostConnectException e)
                {
                    // No internet, do nothing.
                }
                catch (IOException | URISyntaxException | JsonParseException e)
                {
                    Core.LOG.warning("API player settings fetch failed for " + task.playerName + ": " + e);
                }
                finally
                {
                    synchronized (loadingPlayersSet)
                    {
                        loadingPlayersSet.remove(task.playerName);
                        if (failed)
                            retryTimes.put(task.playerName, System.currentTimeMillis() + retryDelayMs);
                        else
                            retryTimes.remove(task.playerName);
                    }
                }
            }
            catch (InterruptedException e)
            {
                Core.LOG.warning("AssetDownloader has been interrupted");
                return;
            }
        }
    }

    private void performTask(PlayerSettingsTask task) throws IOException, URISyntaxException
    {
        Map<String, String> params = new HashMap<>();
        params.put("username", task.playerName);
        if (task.uuid != null)
        {
            params.put("uuid", task.uuid.toString());
        }

        PlayerSettingsResponse response = ConnectionHelper.INSTANCE.sendGetRequest(new URL(apiUrl + "/api/accessory"), params, PlayerSettingsResponse.class);
        if (response == null || response.getSettings() == null)
        {
            throw new JsonParseException("The response has no settings");
        }

        SupporterContent.registerPlayerAccessorySettings(task.playerName, response);

        Core.LOG.info("Player settings fetch successful");
    }

    public void fetchSettingsForPlayer(String playerName, UUID uuid)
    {
        synchronized (loadingPlayersSet)
        {
            Long retryTime = retryTimes.get(playerName);
            if (retryTime != null && System.currentTimeMillis() < retryTime)
            {
                return;
            }

            if (!loadingPlayersSet.contains(playerName))
            {
                playerSettingsTasks.add(new PlayerSettingsTask(playerName, uuid));
                loadingPlayersSet.add(playerName);
            }
        }
    }
}
