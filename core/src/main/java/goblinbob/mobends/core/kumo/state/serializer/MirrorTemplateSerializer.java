package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.template.MirrorTemplate;

import java.lang.reflect.Type;
import java.util.List;

/** A layer's mirror rule: {@code {"@when": <condition>, "pairs": [["leftArm", "rightArm"], ...]}}. */
public class MirrorTemplateSerializer implements JsonDeserializer<MirrorTemplate>
{

    private static final Type PAIRS = new TypeToken<List<List<String>>>() {}.getType();

    @Override
    public MirrorTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = JsonReading.object(json, "A mirror rule");
        if (object.has("negate"))
        {
            throw new JsonParseException("A mirror rule no longer negates inputs (\"negate\"): an item that follows a world direction, "
                    + "such as the head turned by headYaw, isn't mirrored, or only swaps sides (\"@swapSides\").");
        }
        JsonObject fields = JsonReading.fields(object, "A mirror rule", JsonReading.with(JsonReading.same("pairs"), "@when", "when"));
        MirrorTemplate mirror = new MirrorTemplate();
        if (fields.has("when") && !fields.get("when").isJsonNull())
        {
            mirror.when = new ExpressionTemplate(fields.get("when"));
        }
        if (fields.has("pairs"))
        {
            mirror.pairs = context.deserialize(fields.get("pairs"), PAIRS);
        }
        return mirror;
    }

}
