package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.template.DampingTemplate;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * {"@default": 0.3, "rightArm": 0.8, "root": [0.3, 0.6, 0.3]}; a bone's rate can also be an expression
 * (a name or an operation), evaluated every frame.
 */
public class DampingTemplateSerializer implements JsonDeserializer<DampingTemplate>
{

    @Override
    public DampingTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        DampingTemplate damping = new DampingTemplate();
        if (json.isJsonPrimitive())
        {
            damping.entries.put(DampingTemplate.DEFAULT, new float[] { JsonReading.number(json, "A damping") });
            return damping;
        }
        for (Map.Entry<String, JsonElement> entry : JsonReading.object(json, "A damping").entrySet())
        {
            if (JsonReading.COMMENT.equals(entry.getKey()))
            {
                continue;
            }
            JsonReading.requireBoneOrDefault(entry.getKey(), "A damping");
            JsonElement value = entry.getValue();
            if (value.isJsonArray())
            {
                JsonArray array = value.getAsJsonArray();
                float[] values = new float[array.size()];
                for (int i = 0; i < values.length; i++)
                {
                    values[i] = array.get(i).isJsonNull() ? Float.NaN : JsonReading.number(array.get(i), "The damping of '" + entry.getKey() + "'");
                }
                damping.entries.put(entry.getKey(), values);
            }
            else if (value.isJsonObject() || (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()))
            {
                damping.dynamic.put(entry.getKey(), new ExpressionTemplate(value));
            }
            else
            {
                damping.entries.put(entry.getKey(), new float[] { JsonReading.number(value, "The damping of '" + entry.getKey() + "'") });
            }
        }
        return damping;
    }

}
