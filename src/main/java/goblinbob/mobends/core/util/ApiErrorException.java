package goblinbob.mobends.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * An error status from the API, with the {@code {"error": "<code>", "message": "..."}} body it responds with. The
 * code is null if the body isn't one.
 */
public class ApiErrorException extends IOException
{

    private final int status;
    private final String error;

    public ApiErrorException(int status, String error, String message)
    {
        super("HTTP " + status + (error != null ? " " + error : "") + (message != null ? ": " + message : ""));
        this.status = status;
        this.error = error;
    }

    public int getStatus()
    {
        return status;
    }

    public String getError()
    {
        return error;
    }

    static ApiErrorException read(int status, InputStream errorStream)
    {
        if (errorStream == null)
        {
            return new ApiErrorException(status, null, null);
        }

        try (Reader reader = new InputStreamReader(errorStream, StandardCharsets.UTF_8))
        {
            Body body = new Gson().fromJson(reader, Body.class);
            return body == null
                    ? new ApiErrorException(status, null, null)
                    : new ApiErrorException(status, body.error, body.message);
        }
        catch (IOException | JsonParseException e)
        {
            return new ApiErrorException(status, null, null);
        }
    }

    private static class Body
    {
        String error;
        String message;
    }

}
