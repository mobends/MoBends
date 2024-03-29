package goblinbob.mobends.core.connection;

import goblinbob.mobends.core.supporters.SupporterContent;
import goblinbob.mobends.core.util.ConnectionHelper;
import goblinbob.mobends.standard.main.MoBends;
import org.apache.http.conn.HttpHostConnectException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PlayerSettingsDownloader implements Runnable
{
    private final String apiUrl;
    private final Set<String> loadingPlayersSet = new HashSet<>();
    private BlockingQueue<PlayerSettingsTask> playerSettingsTasks = new LinkedBlockingQueue<>();

    public PlayerSettingsDownloader(String apiUrl)
    {
        this.apiUrl = apiUrl;
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

                try
                {
                    performTask(task);
                }
                catch (HttpHostConnectException e)
                {
                    // No internet, do nothing.
                }
                catch(IOException|URISyntaxException e)
                {
                    MoBends.LOG.warning("API player settings fetch failed.");
                    e.printStackTrace();
                }

                synchronized (loadingPlayersSet)
                {
                    loadingPlayersSet.remove(task.playerName);
                }
            }
            catch (InterruptedException e)
            {
                MoBends.LOG.warning("AssetDownloader has been interrupted");
                return;
            }
        }
    }

    private void performTask(PlayerSettingsTask task) throws IOException, URISyntaxException
    {
        Map<String, String> params = new HashMap<>();
        params.put("username", task.playerName);

        PlayerSettingsResponse response = ConnectionHelper.INSTANCE.sendGetRequest(new URL(apiUrl + "/api/accessory"), params, PlayerSettingsResponse.class);

        SupporterContent.registerPlayerAccessorySettings(task.playerName, response);

        MoBends.LOG.info("Player settings fetch successful");
    }

    public void fetchSettingsForPlayer(String playerName)
    {
        synchronized (loadingPlayersSet)
        {
            if (!loadingPlayersSet.contains(playerName))
            {
                playerSettingsTasks.add(new PlayerSettingsTask(playerName));
                loadingPlayersSet.add(playerName);
            }
        }
    }
}
