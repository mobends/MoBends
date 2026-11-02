package goblinbob.mobends.test.connection;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;
import goblinbob.mobends.core.util.ApiErrorException;
import goblinbob.mobends.core.util.ConnectionHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * The API answers errors with {@code {"error": "<code>", "message": "..."}}, which the account link turns into
 * messages for the player.
 */
public class ApiErrorTest
{

    private HttpServer server;
    private int status;
    private String response;
    private String method;
    private String authorization;

    @Before
    public void startServer() throws IOException
    {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            method = exchange.getRequestMethod();
            authorization = exchange.getRequestHeaders().getFirst("Authorization");
            byte[] body = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
            try (OutputStream os = exchange.getResponseBody())
            {
                os.write(body);
            }
        });
        server.start();
    }

    @After
    public void stopServer()
    {
        server.stop(0);
    }

    private <T> T post(Class<T> responseClass) throws IOException
    {
        URL url = new URL("http://127.0.0.1:" + server.getAddress().getPort() + "/api/minecraft-link/start");
        JsonObject body = new JsonObject();
        body.addProperty("code", "ABCD-EFGH");
        return ConnectionHelper.sendPostRequest(url, body, responseClass, 5000);
    }

    private <T> T sendAuthorized(String method, Class<T> responseClass) throws IOException
    {
        URL url = new URL("http://127.0.0.1:" + server.getAddress().getPort() + "/api/minecraft-link/session");
        return ConnectionHelper.sendAuthorizedRequest(method, url, "secret-token", responseClass, 5000);
    }

    private static class Account
    {
        String displayName;
    }

    @Test
    public void readsTheResponse() throws IOException
    {
        status = 200;
        response = "{\"displayName\": \"Żółw\"}";

        assertEquals("Żółw", post(Account.class).displayName);
    }

    @Test
    public void readsTheErrorCode()
    {
        status = 404;
        response = "{\"error\": \"unknown_code\", \"message\": \"The code is unknown\"}";

        try
        {
            post(Account.class);
            fail("Expected an ApiErrorException");
        }
        catch (ApiErrorException e)
        {
            assertEquals(404, e.getStatus());
            assertEquals("unknown_code", e.getError());
        }
        catch (IOException e)
        {
            fail("Expected an ApiErrorException, got " + e);
        }
    }

    @Test
    public void toleratesAnErrorWithoutABody()
    {
        status = 502;
        response = "<html>Bad Gateway</html>";

        try
        {
            post(Account.class);
            fail("Expected an ApiErrorException");
        }
        catch (ApiErrorException e)
        {
            assertEquals(502, e.getStatus());
            assertNull(e.getError());
        }
        catch (IOException e)
        {
            fail("Expected an ApiErrorException, got " + e);
        }
    }

    @Test
    public void authorizesWithTheToken() throws IOException
    {
        status = 200;
        response = "{\"displayName\": \"iwoplaza\"}";

        assertEquals("iwoplaza", sendAuthorized("GET", Account.class).displayName);
        assertEquals("GET", method);
        assertEquals("Bearer secret-token", authorization);
    }

    @Test
    public void readsNoContentAsNull() throws IOException
    {
        status = 204;
        response = "";

        assertNull(sendAuthorized("DELETE", Void.class));
        assertEquals("DELETE", method);
    }

    @Test
    public void readsAnUnknownToken()
    {
        status = 401;
        response = "{\"error\": \"invalid_token\", \"message\": \"Unknown or revoked token\"}";

        try
        {
            sendAuthorized("GET", Account.class);
            fail("Expected an ApiErrorException");
        }
        catch (ApiErrorException e)
        {
            assertEquals(401, e.getStatus());
            assertEquals("invalid_token", e.getError());
        }
        catch (IOException e)
        {
            fail("Expected an ApiErrorException, got " + e);
        }
    }

}
