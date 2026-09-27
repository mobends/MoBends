package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;

import java.lang.reflect.Type;

/**
 * Checks an animator's {@code formatVersion} before reading it. This is where a file written for
 * an older format would be upgraded, on the JSON, one version at a time; for now there is only
 * one format, so anything else is refused with a message saying why.
 */
public class AnimatorTemplateSerializer implements JsonDeserializer<AnimatorTemplate>
{

    @Override
    public AnimatorTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = JsonReading.object(json, "An animator");
        if (!object.has("formatVersion"))
        {
            throw new JsonParseException(String.format("An animator needs a \"formatVersion\" (the current one is %d).", AnimatorTemplate.FORMAT_VERSION));
        }
        int version = (int) JsonReading.number(object.get("formatVersion"), "An animator's \"formatVersion\"");
        if (version > AnimatorTemplate.FORMAT_VERSION)
        {
            throw new JsonParseException(String.format("The animator was written for format %d, which is newer than this version of Mo' Bends reads (%d): update Mo' Bends.", version, AnimatorTemplate.FORMAT_VERSION));
        }
        if (version < AnimatorTemplate.FORMAT_VERSION)
        {
            throw new JsonParseException(String.format("The animator was written for format %d, which this version of Mo' Bends no longer reads (it reads %d).", version, AnimatorTemplate.FORMAT_VERSION));
        }
        return KumoSerializer.INSTANCE.animatorGson.fromJson(object, AnimatorTemplate.class);
    }

}
