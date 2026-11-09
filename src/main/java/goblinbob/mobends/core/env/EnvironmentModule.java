package goblinbob.mobends.core.env;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import goblinbob.mobends.MoBends;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The mod's environment: where the mobends.com API is. It's {@link #DEFAULT_API_URL}, unless the
 * optional {@code config/mobends/env.json} says otherwise ({@code {"apiUrl": "http://localhost:3000"}}),
 * for development against a local backend (1.12's {@code EnvironmentModule}).
 */
public final class EnvironmentModule {
    public static final String DEFAULT_API_URL = "https://mobends.com";

    private static volatile String apiUrl = DEFAULT_API_URL;

    private EnvironmentModule() {
    }

    /** Reads {@code config/mobends/env.json}, if there is one. Once, at start-up. */
    public static void load(Path configDir) {
        Path file = configDir.resolve(MoBends.MOD_ID).resolve("env.json");
        if (!Files.isRegularFile(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject env = new Gson().fromJson(reader, JsonObject.class);
            JsonElement url = env == null ? null : env.get("apiUrl");
            if (url != null && url.isJsonPrimitive() && !url.getAsString().isBlank()) {
                String value = url.getAsString().trim();
                apiUrl = value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
                MoBends.LOGGER.info("Using the Mo' Bends API at {} ({})", apiUrl, file);
            }
        } catch (IOException | JsonParseException e) {
            MoBends.LOGGER.warn("Couldn't read {}: {}", file, e.toString());
        }
    }

    /** The API's base URL, without a trailing slash. */
    public static String apiUrl() {
        return apiUrl;
    }
}
