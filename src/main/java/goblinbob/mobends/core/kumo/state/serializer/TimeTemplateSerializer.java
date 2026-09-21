package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.state.template.TimeTemplate;

import java.lang.reflect.Type;

/** "elapsed", a number (speed over elapsed ticks), or {"variable": ..., "scale": ..., "offset": ...}. */
public class TimeTemplateSerializer implements JsonDeserializer<TimeTemplate>
{

    @Override
    public TimeTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        TimeTemplate time = new TimeTemplate();
        if (json.isJsonPrimitive())
        {
            JsonPrimitive primitive = json.getAsJsonPrimitive();
            if (primitive.isNumber())
            {
                time.scale = primitive.getAsFloat();
            }
            else if (!"elapsed".equalsIgnoreCase(primitive.getAsString()))
            {
                time.variable = primitive.getAsString();
            }
            return time;
        }
        JsonObject object = json.getAsJsonObject();
        if (object.has("variable")) time.variable = object.get("variable").getAsString();
        if (object.has("scale")) time.scale = object.get("scale").getAsFloat();
        if (object.has("offset")) time.offset = object.get("offset").getAsFloat();
        return time;
    }

}
