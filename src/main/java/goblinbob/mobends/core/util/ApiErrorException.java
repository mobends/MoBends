package goblinbob.mobends.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * An error status from the API, with the {@code {"error": "<code>", "message": "..."}} body it
 * responds with. The code is null if the body isn't one (a plain-text 503, say). A request the API
 * finds malformed comes with {@code {"error": {"issues": [{"message": "..."}]}}} instead: the first
 * issue is the message then.
 */
public class ApiErrorException extends IOException {
    private final int status;
    @Nullable
    private final String error;

    public ApiErrorException(int status, @Nullable String error, @Nullable String message) {
        super("HTTP " + status + (error != null ? " " + error : "") + (message != null ? ": " + message : ""));
        this.status = status;
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    /** The API's error code ({@code unknown_code}, {@code invalid_token}, ...), or null if it gave none. */
    @Nullable
    public String getError() {
        return error;
    }

    static ApiErrorException read(int status, @Nullable String body) {
        if (body == null || body.isBlank()) {
            return new ApiErrorException(status, null, null);
        }
        try {
            JsonElement parsed = JsonParser.parseString(body);
            if (!parsed.isJsonObject()) {
                return new ApiErrorException(status, null, null);
            }
            JsonObject object = parsed.getAsJsonObject();
            JsonElement error = object.get("error");
            String code = error != null && error.isJsonPrimitive() ? error.getAsString() : null;
            String message = string(object.get("message"));
            if (message == null && error != null && error.isJsonObject() && error.getAsJsonObject().get("issues") instanceof JsonArray issues
                && !issues.isEmpty() && issues.get(0).isJsonObject()) {
                message = string(issues.get(0).getAsJsonObject().get("message"));
            }
            return new ApiErrorException(status, code, message);
        } catch (JsonParseException | IllegalStateException e) {
            return new ApiErrorException(status, null, null);
        }
    }

    @Nullable
    private static String string(@Nullable JsonElement element) {
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }
}
