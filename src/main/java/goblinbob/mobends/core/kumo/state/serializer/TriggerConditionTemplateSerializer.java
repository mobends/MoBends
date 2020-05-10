package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TriggerConditionTemplateSerializer implements JsonSerializer<TriggerConditionTemplate>, JsonDeserializer<TriggerConditionTemplate>
{

    private final Map<JsonElement, Type> toDeserialize = new HashMap<>();

    @Override
    public JsonElement serialize(TriggerConditionTemplate src, Type typeOfSrc, JsonSerializationContext context)
    {
        return KumoSerializer.INSTANCE.gson.toJsonTree(src);
    }

    @Override
    public TriggerConditionTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        final Gson gson = new Gson();

        if (toDeserialize.containsKey(json))
        {
            return gson.fromJson(json, toDeserialize.remove(json));
        }

        TriggerConditionTemplate abstractTriggerCondition = gson.fromJson(json, TriggerConditionTemplate.class);
        String typeName = abstractTriggerCondition.getType();

        if (typeName == null)
            return null;

        Type templateType = TriggerConditionRegistry.instance.getTemplateClass(typeName);
        if (templateType == null)
            return null;

        toDeserialize.put(json, templateType);

        return KumoSerializer.INSTANCE.gson.fromJson(json, templateType);
    }

}
