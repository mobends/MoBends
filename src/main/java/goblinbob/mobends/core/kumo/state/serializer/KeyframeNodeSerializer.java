package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.keyframe.KeyframeNodeRegistry;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;

import java.lang.reflect.Type;

public class KeyframeNodeSerializer implements JsonSerializer<KeyframeNodeTemplate>, JsonDeserializer<KeyframeNodeTemplate>
{

    @Override
    public JsonElement serialize(KeyframeNodeTemplate src, Type typeOfSrc, JsonSerializationContext context)
    {
        return KumoSerializer.INSTANCE.keyframeNodeGson.toJsonTree(src, KeyframeNodeRegistry.INSTANCE.getTemplateClass(src.getType()));
    }

    @Override
    public KeyframeNodeTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        String type = "core:standard";
        if (json.isJsonObject() && json.getAsJsonObject().has("type"))
        {
            type = json.getAsJsonObject().get("type").getAsString();
        }
        else if (json.isJsonObject() && json.getAsJsonObject().has("pose"))
        {
            type = "core:pose";
        }

        Type templateType = KeyframeNodeRegistry.INSTANCE.getTemplateClass(type);
        if (templateType == null)
        {
            throw new JsonParseException(String.format("A non-existent KeyframeNode type was specified: %s", type));
        }

        KeyframeNodeTemplate template = KumoSerializer.INSTANCE.keyframeNodeGson.fromJson(json, templateType);
        template.setType(type);
        return template;
    }

}
