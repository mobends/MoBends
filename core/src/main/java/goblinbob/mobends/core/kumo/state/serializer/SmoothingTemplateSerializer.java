package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.template.SmoothingTemplate;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * {"@default": 3, "rightArm": 1.25, "root": [3, 1.7, 3]}, in ticks (0: at once, never negative); a
 * bone's smoothing can be its rotation's and position's apart: {"root": {"rotation": 1.67,
 * "position": [3, 1.7, 3]}}. In an item's {@code @smoothing} ({@link #ITEM}) it can also be an
 * expression (a name or an operation), evaluated every frame; a node's or a layer's smoothing is
 * numbers only.
 */
public class SmoothingTemplateSerializer implements JsonDeserializer<SmoothingTemplate>
{

    /** An item's {@code @smoothing}, which takes expressions. */
    public static final SmoothingTemplateSerializer ITEM = new SmoothingTemplateSerializer(true);

    private final boolean expressions;

    /** A node's or a layer's smoothing: numbers only. */
    public SmoothingTemplateSerializer()
    {
        this(false);
    }

    private SmoothingTemplateSerializer(boolean expressions)
    {
        this.expressions = expressions;
    }

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
            String what = "The smoothing of '" + entry.getKey() + "'";
            if (value.isJsonArray())
            {
                smoothing.entries.put(entry.getKey(), values(value, what));
            }
            else if (isSplit(value))
            {
                if (SmoothingTemplate.DEFAULT.equals(entry.getKey()))
                {
                    throw new JsonParseException("A smoothing's \"@default\" is one number of ticks.");
                }
                for (Map.Entry<String, JsonElement> part : value.getAsJsonObject().entrySet())
                {
                    if ("rotation".equals(part.getKey()))
                    {
                        smoothing.rotations.put(entry.getKey(), new float[] { ticks(part.getValue(), what + "'s rotation") });
                    }
                    else if ("position".equals(part.getKey()))
                    {
                        smoothing.positions.put(entry.getKey(), part.getValue().isJsonArray()
                                ? values(part.getValue(), what + "'s position")
                                : new float[] { ticks(part.getValue(), what + "'s position") });
                    }
                }
            }
            else if (value.isJsonObject() || (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()))
            {
                if (!expressions)
                {
                    throw new JsonParseException(what + " is an expression: a node's or a layer's smoothing is numbers of ticks. "
                            + "An item's \"@smoothing\" can be an expression.");
                }
                smoothing.dynamic.put(entry.getKey(), new ExpressionTemplate(value));
            }
            else
            {
                smoothing.entries.put(entry.getKey(), new float[] { ticks(value, "The smoothing of '" + entry.getKey() + "'") });
            }
        }
        return smoothing;
    }

    /** {"rotation": ..., "position": ...} (either may be left out), rather than an operation. */
    private static boolean isSplit(JsonElement value)
    {
        if (!value.isJsonObject() || value.getAsJsonObject().size() == 0)
        {
            return false;
        }
        for (Map.Entry<String, JsonElement> part : value.getAsJsonObject().entrySet())
        {
            if (!"rotation".equals(part.getKey()) && !"position".equals(part.getKey()))
            {
                return false;
            }
        }
        return true;
    }

    private static float[] values(JsonElement json, String what)
    {
        JsonArray array = json.getAsJsonArray();
        float[] values = new float[array.size()];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = array.get(i).isJsonNull() ? Float.NaN : ticks(array.get(i), what);
        }
        return values;
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
