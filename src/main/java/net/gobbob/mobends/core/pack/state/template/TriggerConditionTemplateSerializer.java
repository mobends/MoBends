package net.gobbob.mobends.core.pack.state.template;

import com.google.gson.*;
import net.gobbob.mobends.core.pack.PackDataProvider;
import net.gobbob.mobends.core.pack.state.condition.TriggerConditionRegistry;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TriggerConditionTemplateSerializer implements JsonSerializer<TriggerConditionTemplate>, JsonDeserializer<TriggerConditionTemplate>
{

    private final Map<JsonElement, Type> toDeserialize = new HashMap<>();

    @Override
    public JsonElement serialize(TriggerConditionTemplate src, Type typeOfSrc, JsonSerializationContext context)
    {
        return PackDataProvider.INSTANCE.gson.toJsonTree(src);
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

        return PackDataProvider.INSTANCE.gson.fromJson(json, templateType);
    }

}
