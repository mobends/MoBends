package goblinbob.mobends.core.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

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
        builder.registerTypeAdapter(Color.class, new ColorAdapter());
        this.gson = builder.create();
    }

    public <T> T sendGetRequest(URL url, Map<String, String> params, Class<T> responseClass) throws IOException, URISyntaxException
    {
        HttpGet request = new HttpGet();

        URIBuilder uriBuilder = new URIBuilder(url.toURI());
        for (Map.Entry<String, String> entry : params.entrySet())
        {
            uriBuilder.addParameter(entry.getKey(), entry.getValue());
        }

        request.setURI(uriBuilder.build());

        try (CloseableHttpResponse response = httpClient.execute(request))
        {
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // return it as a String
                return gson.fromJson(EntityUtils.toString(entity), responseClass);
            }
        }

        return null;
    }

    public static <T> T sendPostRequest(URL url, JsonObject body, Class<T> responseClass) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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

        // Response
        BufferedReader json = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        T response = new Gson().fromJson(json, responseClass);

        return response;
    }
}
