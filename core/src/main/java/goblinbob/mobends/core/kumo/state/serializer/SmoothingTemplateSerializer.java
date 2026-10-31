package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.template.SmoothingTemplate;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * {"@default": 3, "rightArm": 1.25, "root": [3, 1.7, 3]}, in ticks (0: at once, never negative); a
 * bone's smoothing can also be an expression (a name or an operation), evaluated every frame.
 */
public class SmoothingTemplateSerializer implements JsonDeserializer<SmoothingTemplate>
{

    @Override
    public SmoothingTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        SmoothingTemplate smoothing = new SmoothingTemplate();
        if (json.isJsonPrimitive())
        {
            smoothing.entries.put(SmoothingTemplate.DEFAULT, new float[] { ticks(json, "A smoothing") });
            return smoothing;
        }
        for (Map.Entry<String, JsonElement> entry : JsonReading.object(json, "A smoothing").entrySet())
        {
            if (JsonReading.COMMENT.equals(entry.getKey()))
            {
                continue;
            }
            JsonReading.requireBoneOrDefault(entry.getKey(), "A smoothing");
            JsonElement value = entry.getValue();
            if (value.isJsonArray())
            {
                JsonArray array = value.getAsJsonArray();
                float[] values = new float[array.size()];
                for (int i = 0; i < values.length; i++)
                {
                    values[i] = array.get(i).isJsonNull() ? Float.NaN : ticks(array.get(i), "The smoothing of '" + entry.getKey() + "'");
                }
                smoothing.entries.put(entry.getKey(), values);
            }
            else if (value.isJsonObject() || (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()))
            {
                smoothing.dynamic.put(entry.getKey(), new ExpressionTemplate(value));
            }
            else
            {
                smoothing.entries.put(entry.getKey(), new float[] { ticks(value, "The smoothing of '" + entry.getKey() + "'") });
            }
        }
        return smoothing;
    }

    private static float ticks(JsonElement json, String what)
    {
        float ticks = JsonReading.number(json, what);
        if (ticks < 0)
        {
            throw new JsonParseException(what + " is in ticks, and can't be negative (0 jumps at once).");
        }
        return ticks;
    }

}
