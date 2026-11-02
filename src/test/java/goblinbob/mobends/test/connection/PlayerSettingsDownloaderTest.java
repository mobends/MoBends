package goblinbob.mobends.test.connection;

import com.sun.net.httpserver.HttpServer;
import goblinbob.mobends.core.connection.PlayerSettingsDownloader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * The renderer asks for a player's accessory settings every frame until they arrive, so a failed fetch must neither
 * stop the downloader nor be retried right away.
 */
public class PlayerSettingsDownloaderTest
{

    private static final long RETRY_DELAY_MS = 500;

    private HttpServer server;
    private Thread thread;
    private PlayerSettingsDownloader downloader;
    /** The username of every request, in order. */
    private final BlockingQueue<String> requests = new LinkedBlockingQueue<>();

    @Before
    public void start() throws IOException
    {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/accessory", exchange -> {
            String username = exchange.getRequestURI().getQuery().replaceAll(".*username=([^&]*).*", "$1");
            int status;
            String body;
            switch (username)
            {
                case "Unavailable":
                    // The API before Discord's role data has loaded.
                    status = 503;
                    body = "503 Service Unavailable";
                    break;
                case "Malformed":
                    status = 200;
                    body = "not the settings";
                    break;
                case "Empty":
                    status = 200;
                    body = "{}";
                    break;
                default:
                    status = 500;
                    body = "{\"error\": \"internal_error\", \"message\": \"500 Internal Server Error\"}";
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody())
            {
                os.write(bytes);
            }
            requests.add(username);
        });
        server.start();

        downloader = new PlayerSettingsDownloader("http://127.0.0.1:" + server.getAddress().getPort(), RETRY_DELAY_MS);
        thread = new Thread(downloader);
        thread.setDaemon(true);
        thread.start();
    }

    @After
    public void stop()
    {
        thread.interrupt();
        server.stop(0);
    }

    private String nextRequest() throws InterruptedException
    {
        String username = requests.poll(5, TimeUnit.SECONDS);
        assertNotNull("Expected a request", username);
        return username;
    }

    @Test
    public void waitsBeforeFetchingAFailedPlayerAgain() throws InterruptedException
    {
        downloader.fetchSettingsForPlayer("Unavailable", null);
        assertEquals("Unavailable", nextRequest());
        // Lets the downloader handle the failure.
        Thread.sleep(100);

        // Every frame asks again, but only the other player is fetched.
        downloader.fetchSettingsForPlayer("Unavailable", null);
        downloader.fetchSettingsForPlayer("Other", null);
        assertEquals("Other", nextRequest());

        Thread.sleep(RETRY_DELAY_MS + 100);
        downloader.fetchSettingsForPlayer("Unavailable", null);
        assertEquals("Unavailable", nextRequest());
        assertTrue(thread.isAlive());
    }

    @Test
    public void survivesResponsesThatAreNotTheSettings() throws InterruptedException
    {
        downloader.fetchSettingsForPlayer("Malformed", null);
        assertEquals("Malformed", nextRequest());
        downloader.fetchSettingsForPlayer("Empty", null);
        assertEquals("Empty", nextRequest());
        downloader.fetchSettingsForPlayer("Other", null);
        assertEquals("Other", nextRequest());

        Thread.sleep(100);
        assertTrue(thread.isAlive());
    }

}
