package goblinbob.mobends.core.connection;

import goblinbob.mobends.core.Core;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.module.IModule;
import goblinbob.mobends.core.util.ConnectionHelper;
import goblinbob.mobends.core.util.ErrorReporter;
import goblinbob.mobends.core.ModStatics;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

public class ConnectionManager
{
    public static ConnectionManager INSTANCE;
    private static final String DEV_CHECKSUM = "dev";

    /**
     * SHA-256 of the jar the mod was loaded from, as lowercase hex, which tells the API an official release from a
     * fork. {@link #DEV_CHECKSUM} in development, where the mod is loaded from a directory, and null if hashing
     * failed.
     */
    private final String checksum;
    private boolean initialized = false;
    private PingTask pingTask;
    private Thread pingTaskThread;
    private PlayerSettingsDownloader playerSettingsDownloader;
    private Thread playerSettingsDownloaderThread;

    private ConnectionManager(String checksum)
    {
        this.checksum = checksum;
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
            response = ConnectionHelper.sendPostRequest(new URL(apiUrl + "/api/activity/join"), createActivityBody(checksum), JoinResponse.class);

            Core.LOG.info("Ping interval: " + response.pingInterval);
        }
        catch (IOException e)
        {
            ErrorReporter.showErrorToPlayer("Couldn't join the API. Some features may be disabled. " +
                    "Contact the developers if this is a prolonged issue.");
            e.printStackTrace();
            Core.LOG.log(Level.SEVERE, e.getMessage());
            return;
        }

        pingTask = new PingTask(apiUrl, response.pingInterval, checksum);
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

    /**
     * The body shared by the join and ping requests.
     */
    static JsonObject createActivityBody(String checksum)
    {
        JsonObject body = new JsonObject();
        body.addProperty("app", "mobends");
        body.addProperty("version", ModStatics.VERSION_STRING);
        body.addProperty("checksum", checksum);
        return body;
    }

    private static String computeChecksum(File source)
    {
        if (source == null)
        {
            return null;
        }

        if (source.isDirectory())
        {
            return DEV_CHECKSUM;
        }

        try
        {
            return Files.asByteSource(source).hash(Hashing.sha256()).toString();
        }
        catch (IOException e)
        {
            Core.LOG.warning("Couldn't compute the mod's checksum: " + e.getMessage());
            return null;
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
            ConnectionManager.INSTANCE = new ConnectionManager(computeChecksum(event.getSourceFile()));
        }

        @Override
        public void onRefresh()
        {
            ConnectionManager.INSTANCE.setup();
        }
    }
}
