package goblinbob.mobends.core.connection;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import com.google.gson.JsonObject;

import goblinbob.mobends.core.util.ConnectionHelper;
import goblinbob.mobends.core.util.ErrorReporter;
import goblinbob.mobends.standard.main.MoBends;
import goblinbob.mobends.standard.main.ModStatics;

public class ConnectionManager
{
    public static ConnectionManager INSTANCE = new ConnectionManager();

    private static String API_URL = "http://localhost:5000";

    private boolean initialized = false;
    private PingTask pingTask;
    private Thread pingTaskThread;
    private AssetDownloader assetDownloader;
    private Thread assetDownloaderThread;

    private ConnectionManager() {}

    public void setup()
    {
        if (initialized)
        {
            return;
        }

        JoinResponse response = null;

        try
        {
            JsonObject body = new JsonObject();
            body.addProperty("app", "mobends");
            body.addProperty("version", ModStatics.VERSION_STRING);

            response = ConnectionHelper.sendPostRequest(new URL(API_URL + "/api/activity/join"), body, JoinResponse.class);

            MoBends.LOG.info("Ping interval: " + response.pingInterval);
        }
        catch (IOException e)
        {
            ErrorReporter.showErrorToPlayer("Couldn't join the API. Some features may be disabled. " +
                    "Contact the developers if this is a prolonged issue.");
            e.printStackTrace();
            MoBends.LOG.log(Level.SEVERE, e.getMessage());
            return;
        }

        pingTask = new PingTask(API_URL, response.pingInterval);
        pingTaskThread = new Thread(pingTask);
        pingTaskThread.start();

        assetDownloader = new AssetDownloader(API_URL, 10);
        assetDownloaderThread = new Thread(assetDownloader);
        assetDownloaderThread.start();

        initialized = true;
    }

    public void fetchSettingsForPlayer(String playerName)
    {
        if (assetDownloader != null)
        {
            assetDownloader.fetchSettingsForPlayer(playerName);
        }
    }

    private static class JoinResponse
    {
        public float pingInterval;
    }
}
