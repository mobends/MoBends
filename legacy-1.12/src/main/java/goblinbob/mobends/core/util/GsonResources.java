package goblinbob.mobends.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import goblinbob.mobends.core.client.PackTrust;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Reads JSON resources for animation, as {@link PackTrust} allows. Callers cache what they read. */
public final class GsonResources
{

    private GsonResources()
    {
    }

    /**
     * @throws IOException when the resource is missing, or isn't what {@code classOfT} expects
     *                     (the parse error is the cause)
     */
    public static <T> T read(ResourceLocation location, Gson gson, Class<T> classOfT) throws IOException
    {
        try (InputStream stream = PackTrust.open(location))
        {
            T resource = gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), classOfT);
            if (resource == null)
            {
                throw new IOException(location + " is empty");
            }
            return resource;
        }
        catch (JsonParseException | IllegalStateException e)
        {
            throw new IOException(location + ": " + e.getMessage(), e);
        }
    }

    /**
     * Like {@link #read(ResourceLocation, Gson, Class)}, for a file that carries a
     * {@code formatVersion} (see {@link FormatVersion}).
     *
     * @param what the kind of file, for messages
     */
    public static <T> T read(ResourceLocation location, Gson gson, Class<T> classOfT, String what, int formatVersion) throws IOException
    {
        JsonElement json = read(location, gson, JsonElement.class);
        try
        {
            return gson.fromJson(FormatVersion.check(json, what, formatVersion), classOfT);
        }
        catch (JsonParseException | IllegalStateException e)
        {
            throw new IOException(location + ": " + e.getMessage(), e);
        }
    }

}
