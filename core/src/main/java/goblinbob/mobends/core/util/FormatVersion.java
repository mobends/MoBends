package goblinbob.mobends.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * The {@code formatVersion} every Mo' Bends asset file carries (animators, types, extensions, model
 * definitions), each format numbered on its own. This is where a file written for an older format
 * would be upgraded, on the JSON, one version at a time; until a format changes, anything but its
 * current version is refused with a message saying why.
 */
public final class FormatVersion
{

    private FormatVersion()
    {
    }

    /**
     * Checks that {@code json} is an object written for format {@code current}.
     *
     * @param what the kind of file, for messages ("animator", "type file", ...)
     * @throws JsonParseException if it isn't
     */
    public static JsonObject check(JsonElement json, String what, int current)
    {
        if (json == null || !json.isJsonObject())
        {
            throw new JsonParseException(String.format("The %s has to be an object.", what));
        }
        JsonObject object = json.getAsJsonObject();
        JsonElement version = object.get("formatVersion");
        if (version == null)
        {
            throw new JsonParseException(String.format("The %s needs a \"formatVersion\" (the current one is %d).", what, current));
        }
        if (!version.isJsonPrimitive() || !version.getAsJsonPrimitive().isNumber())
        {
            throw new JsonParseException(String.format("The %s's \"formatVersion\" has to be a number.", what));
        }
        int written = version.getAsInt();
        if (written > current)
        {
            throw new JsonParseException(String.format("The %s was written for format %d, which is newer than this version of Mo' Bends reads (%d): update Mo' Bends.", what, written, current));
        }
        if (written < current)
        {
            throw new JsonParseException(String.format("The %s was written for format %d, which this version of Mo' Bends no longer reads (it reads %d).", what, written, current));
        }
        return object;
    }

}
