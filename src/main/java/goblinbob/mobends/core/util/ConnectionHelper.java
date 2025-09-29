package goblinbob.mobends.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import goblinbob.mobends.core.asset.AssetLocation;
import goblinbob.mobends.core.supporters.BindPoint;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.client.utils.URIBuilder;

/**
 * HTTP connection utility that applies SSL certificate bypassing directly to HTTPS connections.
 * This avoids SSL validation issues (like "PKIX path building failed") by immediately
 * bypassing certificate validation for all HTTPS requests, eliminating the need for
 * complex retry logic while maintaining simple, reliable network connectivity.
 */
public class ConnectionHelper {

    public static ConnectionHelper INSTANCE = new ConnectionHelper();
    private Gson gson;

    /**
     * Makes it so we can't instantiate this class.
     */
    private ConnectionHelper() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(Color.class, new ColorAdapter());
        builder.registerTypeAdapter(BindPoint.class, new BindPoint.Adapter());
        builder.registerTypeAdapter(
            AssetLocation.class,
            new AssetLocation.Adapter()
        );
        this.gson = builder.create();
    }

    public Gson getGson() {
        return gson;
    }

    /**
     * Sends a GET request with SSL bypassing applied to HTTPS connections.
     */
    public static <T> T sendGetRequest(
        URL url,
        Map<String, String> params,
        Class<T> responseClass
    ) throws IOException, URISyntaxException {
        // Build URL with query parameters using URIBuilder
        URIBuilder uriBuilder = new URIBuilder(url.toURI());
        for (Map.Entry<String, String> entry : params.entrySet()) {
            uriBuilder.addParameter(entry.getKey(), entry.getValue());
        }

        URL fullUrl = uriBuilder.build().toURL();
        HttpURLConnection connection =
            (HttpURLConnection) fullUrl.openConnection();

        // Apply SSL bypass if this is an HTTPS connection
        if (connection instanceof HttpsURLConnection) {
            SSLHelper.bypassSSLForConnection((HttpsURLConnection) connection);
        }

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000); // 10 seconds
        connection.setReadTimeout(10000); // 10 seconds
        connection.connect();

        // Check response code
        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException(
                "HTTP request failed with response code: " + responseCode
            );
        }

        // Read response
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    connection.getInputStream(),
                    StandardCharsets.UTF_8
                )
            )
        ) {
            return INSTANCE.gson.fromJson(reader, responseClass);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Sends a POST request with SSL bypassing applied to HTTPS connections.
     */
    public static <T> T sendPostRequest(
        URL url,
        JsonObject body,
        Class<T> responseClass
    ) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Apply SSL bypass if this is an HTTPS connection
        if (connection instanceof HttpsURLConnection) {
            SSLHelper.bypassSSLForConnection((HttpsURLConnection) connection);
        }

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000); // 10 seconds
        connection.setReadTimeout(10000); // 10 seconds

        byte[] jsonBytes = INSTANCE.gson
            .toJson(body)
            .getBytes(StandardCharsets.UTF_8);
        int length = jsonBytes.length;

        connection.setFixedLengthStreamingMode(length);
        connection.setRequestProperty(
            "Content-Type",
            "application/json; charset=UTF-8"
        );
        connection.connect();

        // Write request body
        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonBytes);
            os.flush();
        }

        // Check response code
        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException(
                "HTTP POST request failed with response code: " + responseCode
            );
        }

        // Read response
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    connection.getInputStream(),
                    StandardCharsets.UTF_8
                )
            )
        ) {
            T response = INSTANCE.gson.fromJson(reader, responseClass);
            return response;
        } finally {
            connection.disconnect();
        }
    }
}
