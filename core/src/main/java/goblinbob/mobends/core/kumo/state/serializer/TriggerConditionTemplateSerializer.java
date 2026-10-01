package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.condition.NamedCondition;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

import java.lang.reflect.Type;

/** Picks the trigger condition template class by {@code type}; a string is the name of a named condition. */
public class TriggerConditionTemplateSerializer implements JsonDeserializer<TriggerConditionTemplate>
{

    @Override
    public TriggerConditionTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString())
        {
            return new NamedCondition.Template(json.getAsString());
        }
        JsonObject object = JsonReading.object(json, "A trigger condition");
        String typeName = JsonReading.string(object.get("type"), "A trigger condition's \"type\"");
        Type templateType = TriggerConditionRegistry.INSTANCE.getTemplateClass(typeName);
        if (templateType == null)
        {
            throw new JsonParseException(String.format("Unknown trigger condition: '%s'.", typeName));
        }

        if (templateType.equals(TriggerConditionTemplate.class))
        {
            // A condition without parameters (reading it through the leaf Gson would recurse into this adapter).
            TriggerConditionTemplate template = new TriggerConditionTemplate();
            template.type = typeName;
            return template;
        }

        return KumoSerializer.INSTANCE.leafGson.fromJson(json, templateType);
    }

}
