package goblinbob.mobends.core;

import com.google.gson.Gson;
import goblinbob.mobends.core.util.ErrorReporter;
import goblinbob.mobends.core.util.SSLHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import javax.net.ssl.HttpsURLConnection;

public class WebAPI {

    public static WebAPI INSTANCE = new WebAPI();

    private static String apiUrl =
        "https://raw.githubusercontent.com/mobends/mobends-resources/master/static-api.json";

    private boolean initialized = false;
    private APIData data;

    /**
     * Network connection in this class uses direct SSL certificate bypassing for HTTPS URLs.
     * This bypasses SSL validation issues (like "PKIX path building failed") that can occur
     * when connecting to external APIs. The SSL bypass is applied immediately to all HTTPS
     * connections to ensure reliable connectivity without retry logic complexity.
     */
    private void initialize() {
        if (initialized) {
            return;
        }

        try {
            URL url = new URL(apiUrl);
            URLConnection urlConnection = SSLHelper.openConnectionWithSSLBypass(
                url
            );
            HttpURLConnection connection = (HttpURLConnection) urlConnection;

            connection.setRequestMethod("GET");
            connection.connect();

            BufferedReader json = new BufferedReader(
                new InputStreamReader(connection.getInputStream())
            );
            this.data = new Gson().fromJson(json, APIData.class);
        } catch (IOException e) {
            ErrorReporter.showErrorToPlayer(
                "Couldn't fetch the WebAPI data. Some features may be disabled. " +
                    "Contact the developers if this is a prolonged issue."
            );
            e.printStackTrace();
        }

        initialized = true;
    }

    public String getOfficialAnimationEditorUrl() {
        initialize();

        if (data == null) {
            return null;
        }

        return data.officialAnimationEditorUrl;
    }

    private static class APIData {

        String officialAnimationEditorUrl = null;
    }
}
