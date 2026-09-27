package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

import java.lang.reflect.Type;

public class TriggerConditionTemplateSerializer implements JsonSerializer<TriggerConditionTemplate>, JsonDeserializer<TriggerConditionTemplate>
{

    @Override
    public JsonElement serialize(TriggerConditionTemplate src, Type typeOfSrc, JsonSerializationContext context)
    {
        return KumoSerializer.INSTANCE.keyframeNodeGson.toJsonTree(src);
    }

    @Override
    public TriggerConditionTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (!json.isJsonObject() || !json.getAsJsonObject().has("type"))
        {
            throw new JsonParseException("A trigger condition needs a 'type'.");
        }

        String typeName = json.getAsJsonObject().get("type").getAsString();
        Type templateType = TriggerConditionRegistry.instance.getTemplateClass(typeName);

        if (templateType == null)
        {
            throw new JsonParseException(String.format("A non-existent trigger condition type was specified: %s", typeName));
        }

        if (templateType.equals(TriggerConditionTemplate.class))
        {
            // A "pure" condition without parameters.
            TriggerConditionTemplate template = new TriggerConditionTemplate();
            template.type = typeName;
            return template;
        }

        return KumoSerializer.INSTANCE.keyframeNodeGson.fromJson(json, templateType);
    }

}
