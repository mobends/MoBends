package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.Locale;

/** Shared checks of the animator deserializers: malformed input always ends in a {@link JsonParseException}. */
final class JsonReading
{

    private JsonReading() {}

    static JsonObject object(JsonElement json, String what)
    {
        if (json == null || !json.isJsonObject())
        {
            throw new JsonParseException(String.format("%s has to be an object.", what));
        }
        return json.getAsJsonObject();
    }

    static String string(JsonElement json, String what)
    {
        if (json == null || !json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString())
        {
            throw new JsonParseException(String.format("%s has to be a string.", what));
        }
        return json.getAsString();
    }

    static float number(JsonElement json, String what)
    {
        if (json == null || !json.isJsonPrimitive() || !json.getAsJsonPrimitive().isNumber())
        {
            throw new JsonParseException(String.format("%s has to be a number.", what));
        }
        return json.getAsFloat();
    }

    /** The constant of {@code type} named by {@code json}, ignoring case. */
    static <E extends Enum<E>> E enumValue(Class<E> type, JsonElement json, String what)
    {
        String name = string(json, what);
        try
        {
            return Enum.valueOf(type, name.toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException e)
        {
            throw new JsonParseException(String.format("%s: unknown value '%s'.", what, name));
        }
    }

}
