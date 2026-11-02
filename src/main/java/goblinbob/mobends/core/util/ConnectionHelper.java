package goblinbob.mobends.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import goblinbob.mobends.core.asset.AssetLocation;
import goblinbob.mobends.core.supporters.BindPoint;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ConnectionHelper
{
    public static ConnectionHelper INSTANCE = new ConnectionHelper();
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private Gson gson;

    /**
     * Makes it so we can't instantiate this class.
     */
    private ConnectionHelper()
    {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(Color.class, new ColorAdapter().nullSafe());
        builder.registerTypeAdapter(BindPoint.class, new BindPoint.Adapter());
        builder.registerTypeAdapter(AssetLocation.class, new AssetLocation.Adapter());
        this.gson = builder.create();
    }

    public Gson getGson()
    {
        return gson;
    }

    public static <T> T sendGetRequest(URL url, Map<String, String> params, Class<T> responseClass) throws IOException, URISyntaxException
    {
        HttpGet request = new HttpGet();

        URIBuilder uriBuilder = new URIBuilder(url.toURI());
        for (Map.Entry<String, String> entry : params.entrySet())
        {
            uriBuilder.addParameter(entry.getKey(), entry.getValue());
        }

        request.setURI(uriBuilder.build());

        try (CloseableHttpResponse response = INSTANCE.httpClient.execute(request))
        {
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // return it as a String
                return INSTANCE.gson.fromJson(EntityUtils.toString(entity), responseClass);
            }
        }

        return null;
    }

    public static <T> T sendPostRequest(URL url, JsonObject body, Class<T> responseClass) throws IOException
    {
        return sendPostRequest(url, body, responseClass, 0);
    }

    /**
     * Posts the body as JSON and reads the response as the given class.
     *
     * @param timeoutMs How long to wait to connect, and then for the response, or 0 to wait forever.
     * @throws ApiErrorException If the API responds with an error status.
     */
    public static <T> T sendPostRequest(URL url, JsonObject body, Class<T> responseClass, int timeoutMs) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        byte[] out = (new Gson()).toJson(body).getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        connection.setFixedLengthStreamingMode(length);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.connect();

        try (OutputStream os = connection.getOutputStream())
        {
            os.write(out);
        }

        return readResponse(connection, responseClass);
    }

    /**
     * Sends a request without a body, authorized with the token as a bearer, and reads the response as the given
     * class.
     *
     * @param method E.g. "GET" or "DELETE".
     * @param timeoutMs How long to wait to connect, and then for the response, or 0 to wait forever.
     * @return The response, or null if it has no body (e.g. 204 No Content).
     * @throws ApiErrorException If the API responds with an error status.
     */
    public static <T> T sendAuthorizedRequest(String method, URL url, String token, Class<T> responseClass, int timeoutMs) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", "Bearer " + token);

        return readResponse(connection, responseClass);
    }

    private static <T> T readResponse(HttpURLConnection connection, Class<T> responseClass) throws IOException
    {
        int status = connection.getResponseCode();
        if (status >= 400)
        {
            throw ApiErrorException.read(status, connection.getErrorStream());
        }
        if (status == HttpURLConnection.HTTP_NO_CONTENT)
        {
            return null;
        }

        try (BufferedReader json = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)))
        {
            return INSTANCE.gson.fromJson(json, responseClass);
        }
    }
}
