package goblinbob.mobends.core.connection;

import com.google.gson.JsonObject;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.module.IModule;
import goblinbob.mobends.core.util.ConnectionHelper;
import goblinbob.mobends.core.util.ErrorReporter;
import goblinbob.mobends.standard.main.MoBends;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

public class ConnectionManager
{
    public static ConnectionManager INSTANCE;

    private boolean initialized = false;
    private PingTask pingTask;
    private Thread pingTaskThread;
    private PlayerSettingsDownloader playerSettingsDownloader;
    private Thread playerSettingsDownloaderThread;

    private ConnectionManager()
    {
        this.setup();
    }

    public void setup()
    {
        if (initialized)
        {
            return;
        }

        String apiUrl = EnvironmentModule.getConfig().getApiUrl();

        JoinResponse response = null;

        try
        {
            JsonObject body = new JsonObject();
            body.addProperty("app", "mobends");
            body.addProperty("version", ModStatics.VERSION_STRING);

            response = ConnectionHelper.sendPostRequest(new URL(apiUrl + "/api/activity/join"), body, JoinResponse.class);

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

        pingTask = new PingTask(apiUrl, response.pingInterval);
        pingTaskThread = new Thread(pingTask);
        pingTaskThread.setDaemon(true);
        pingTaskThread.start();

        playerSettingsDownloader = new PlayerSettingsDownloader(apiUrl);
        playerSettingsDownloaderThread = new Thread(playerSettingsDownloader);
        playerSettingsDownloaderThread.setDaemon(true);
        playerSettingsDownloaderThread.start();

        initialized = true;
    }

    public void fetchSettingsForPlayer(String playerName)
    {
        if (playerSettingsDownloader != null)
        {
            playerSettingsDownloader.fetchSettingsForPlayer(playerName);
        }
    }

    private static class JoinResponse
    {
        public float pingInterval;
    }

    public static class Factory implements IModule
    {
        @Override
        public void preInit(FMLPreInitializationEvent event)
        {
            ConnectionManager.INSTANCE = new ConnectionManager();
        }

        @Override
        public void onRefresh()
        {
            ConnectionManager.INSTANCE.setup();
        }
    }
}
