package goblinbob.mobends.core.connection;

import com.google.gson.JsonObject;
import goblinbob.mobends.core.util.ConnectionHelper;
import goblinbob.mobends.standard.main.MoBends;
import goblinbob.mobends.standard.main.ModStatics;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;

/**
 * Sends a ping request in a specific interval.
 */
public class PingTask implements Runnable
{
    private final String apiUrl;
    /**
     * The delay between pings in seconds.
     */
    private final float interval;

    public PingTask(String apiUrl, float interval)
    {
        this.apiUrl = apiUrl;
        this.interval = interval;
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                sendPing();
                Thread.sleep((long) (this.interval * 1000));
            }
            catch (InterruptedException e)
            {
                MoBends.LOG.warning("PingTask has been interrupted");
                return;
            }
        }
    }

    private void sendPing()
    {
        JsonObject body = new JsonObject();
        body.addProperty("app", "mobends");
        body.addProperty("version", ModStatics.VERSION_STRING);

        try
        {
            ConnectionHelper.sendPostRequest(new URL(apiUrl + "/api/activity/ping"), body, PingResponse.class);
        }
        catch (ConnectException e)
        {
            // No internet connection, do nothing.
        }
        catch(IOException e)
        {
            MoBends.LOG.warning("API ping failed.");
        }
    }

    private static class PingResponse
    {
        public boolean success;
    }
}
