package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.util.FormatVersion;

import java.lang.reflect.Type;
import java.util.Map;

/** Checks an animator's {@code formatVersion} (see {@link FormatVersion}) before reading it. */
public class AnimatorTemplateSerializer implements JsonDeserializer<AnimatorTemplate>
{

    private static final Map<String, String> KEYS = JsonReading.with(JsonReading.same("formatVersion", "extends", "layers"), "@expressions", "expressions");

    @Override
    public AnimatorTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = FormatVersion.check(json, "animator", AnimatorTemplate.FORMAT_VERSION);
        return KumoSerializer.INSTANCE.animatorGson.fromJson(JsonReading.fields(object, "An animator", KEYS), AnimatorTemplate.class);
    }

}
