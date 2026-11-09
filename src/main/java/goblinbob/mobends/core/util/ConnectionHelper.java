package goblinbob.mobends.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.env.EnvironmentModule;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Talks to the mobends.com API (see docs/api-communication.md): JSON in and out, over one shared
 * {@link HttpClient}. Every call blocks, so it's made off the render thread ({@link #runInBackground}),
 * and what it brings is handed back to the render thread ({@link #onRenderThread}).
 */
public final class ConnectionHelper {
    public static final Gson GSON = new Gson();

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(TIMEOUT)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    private static final String USER_AGENT = "MoBends/" + MoBends.VERSION + " (Minecraft " + MoBends.MINECRAFT + ")";

    private ConnectionHelper() {
    }

    /** The API's {@code path} ({@code /api/activity/join}), at the API's URL ({@link EnvironmentModule}). */
    public static URI endpoint(String path) {
        return URI.create(EnvironmentModule.apiUrl() + path);
    }

    /**
     * Gets {@code path} with {@code params} in its query, and reads the response as {@code type}.
     *
     * @return the response, or null if it has no body
     * @throws ApiErrorException if the API responds with an error status, whatever its body
     */
    @Nullable
    public static <T> T get(String path, Map<String, String> params, Class<T> type) throws IOException {
        StringJoiner query = new StringJoiner("&", "?", "");
        query.setEmptyValue("");
        for (Map.Entry<String, String> param : params.entrySet()) {
            query.add(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
        }
        return send(request(URI.create(endpoint(path) + query.toString())).GET().build(), type);
    }

    /**
     * Posts {@code body} as JSON to {@code path}, and reads the response as {@code type}.
     *
     * @throws ApiErrorException if the API responds with an error status
     */
    @Nullable
    public static <T> T post(String path, JsonObject body, Class<T> type) throws IOException {
        HttpRequest request = request(endpoint(path))
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body), StandardCharsets.UTF_8))
            .build();
        return send(request, type);
    }

    /**
     * Sends a request without a body to {@code path}, authorized with {@code token} as a bearer,
     * and reads the response as {@code type}.
     *
     * @param method e.g. {@code GET} or {@code DELETE}
     * @return the response, or null if it has no body (204 No Content)
     * @throws ApiErrorException if the API responds with an error status
     */
    @Nullable
    public static <T> T sendAuthorized(String method, String path, String token, Class<T> type) throws IOException {
        HttpRequest request = request(endpoint(path))
            .header("Authorization", "Bearer " + token)
            .method(method, HttpRequest.BodyPublishers.noBody())
            .build();
        return send(request, type);
    }

    /** Downloads {@code uri} (an asset, the static API) whole. */
    public static byte[] download(URI uri) throws IOException {
        HttpResponse<byte[]> response = exchange(request(uri).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() >= 400) {
            throw new ApiErrorException(response.statusCode(), null, uri.toString());
        }
        return response.body();
    }

    /** Whether {@code e} only says the API can't be reached (no connection): nothing worth a stack trace. */
    public static boolean isOffline(IOException e) {
        return e instanceof ConnectException || e instanceof UnknownHostException || e instanceof HttpTimeoutException || e.getCause() instanceof UnknownHostException;
    }

    /** Runs {@code task} on a daemon thread of its own, named {@code Mo' Bends <name>}. */
    public static void runInBackground(String name, Runnable task) {
        Thread thread = new Thread(task, "Mo' Bends " + name);
        thread.setDaemon(true);
        thread.start();
    }

    /** Runs {@code task} on the render thread, at its next turn (now, if this is the render thread). */
    public static void onRenderThread(Runnable task) {
        Minecraft.getInstance().execute(task);
    }

    private static HttpRequest.Builder request(URI uri) {
        return HttpRequest.newBuilder(uri).timeout(TIMEOUT).header("User-Agent", USER_AGENT).header("Accept", "application/json");
    }

    @Nullable
    private static <T> T send(HttpRequest request, Class<T> type) throws IOException {
        HttpResponse<String> response = exchange(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int status = response.statusCode();
        if (status >= 400) {
            throw ApiErrorException.read(status, response.body());
        }
        String body = response.body();
        if (status == 204 || body == null || body.isBlank() || type == Void.class) {
            return null;
        }
        try {
            return GSON.fromJson(body, type);
        } catch (JsonParseException e) {
            throw new IOException("The API's response to " + request.uri().getPath() + " isn't what was expected: " + e.getMessage(), e);
        }
    }

    private static <B> HttpResponse<B> exchange(HttpRequest request, HttpResponse.BodyHandler<B> handler) throws IOException {
        try {
            return CLIENT.send(request, handler);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Interrupted while calling " + request.uri());
        }
    }
}
