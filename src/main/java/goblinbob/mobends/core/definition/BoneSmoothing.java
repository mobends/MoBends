package goblinbob.mobends.core.definition;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.JsonAdapter;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * A bone's base smoothing in a model definition's {@code smoothing}: the ticks it takes to reach a
 * target in a frame where nothing in the animator smooths it. A number smooths both its rotation and
 * its position; {@code {"rotation": 0.5, "position": [3, 1.7, 3]}} gives them apart (the position
 * one number, or one per axis), and a part left out keeps its default.
 */
@JsonAdapter(BoneSmoothing.Deserializer.class)
public final class BoneSmoothing
{

    /** Ticks for the rotation, or null to keep its default. */
    @Nullable
    public final Float rotation;
    /** Ticks for the position, per axis, or null to keep its default. */
    @Nullable
    public final float[] position;

    public BoneSmoothing(@Nullable Float rotation, @Nullable float[] position)
    {
        this.rotation = rotation;
        this.position = position;
    }

    public static final class Deserializer implements JsonDeserializer<BoneSmoothing>
    {
        @Override
        public BoneSmoothing deserialize(JsonElement json, Type type, JsonDeserializationContext context)
        {
            if (isNumber(json))
            {
                float ticks = ticks(json, "A smoothing");
                return new BoneSmoothing(ticks, new float[] {ticks, ticks, ticks});
            }
            if (!json.isJsonObject())
            {
                throw new JsonParseException("A smoothing is a number of ticks, or {\"rotation\": ..., \"position\": ...}, not " + json + ".");
            }
            Float rotation = null;
            float[] position = null;
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet())
            {
                switch (entry.getKey())
                {
                    case "rotation":
                        if (!isNumber(entry.getValue()))
                        {
                            throw new JsonParseException("A smoothing's \"rotation\" is a number of ticks, not " + entry.getValue() + ".");
                        }
                        rotation = ticks(entry.getValue(), "A smoothing's \"rotation\"");
                        break;
                    case "position":
                        position = position(entry.getValue());
                        break;
                    case "@comment":
                        break;
                    default:
                        throw new JsonParseException("A smoothing takes no '" + entry.getKey() + "' (it takes \"rotation\" and \"position\").");
                }
            }
            return new BoneSmoothing(rotation, position);
        }

        private static float[] position(JsonElement json)
        {
            String what = "A smoothing's \"position\"";
            if (isNumber(json))
            {
                float ticks = ticks(json, what);
                return new float[] {ticks, ticks, ticks};
            }
            if (json.isJsonArray() && json.getAsJsonArray().size() == 3)
            {
                JsonArray array = json.getAsJsonArray();
                float[] ticks = new float[3];
                for (int i = 0; i < 3; i++)
                {
                    if (!isNumber(array.get(i)))
                    {
                        throw new JsonParseException(what + " is a number of ticks or [x, y, z], not " + json + ".");
                    }
                    ticks[i] = ticks(array.get(i), what);
                }
                return ticks;
            }
            throw new JsonParseException(what + " is a number of ticks or [x, y, z], not " + json + ".");
        }

        private static boolean isNumber(JsonElement json)
        {
            return json.isJsonPrimitive() && ((JsonPrimitive) json).isNumber();
        }

        private static float ticks(JsonElement json, String what)
        {
            float ticks = json.getAsFloat();
            if (!(ticks >= 0))
            {
                throw new JsonParseException(what + " can't be negative (" + json + ").");
            }
            return ticks;
        }
    }

}
