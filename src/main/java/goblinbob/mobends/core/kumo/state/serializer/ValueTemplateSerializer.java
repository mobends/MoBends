package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.state.template.ValueTemplate;

import java.lang.reflect.Type;

/** A value is a plain number or an object describing a variable expression. */
public class ValueTemplateSerializer implements JsonDeserializer<ValueTemplate>
{

    @Override
    public ValueTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        ValueTemplate value = new ValueTemplate();
        if (json.isJsonPrimitive())
        {
            value.constant = json.getAsFloat();
            return value;
        }
        JsonObject object = json.getAsJsonObject();
        if (object.has("variable")) value.variable = object.get("variable").getAsString();
        if (object.has("constant")) value.constant = object.get("constant").getAsFloat();
        if (object.has("scale")) value.scale = object.get("scale").getAsFloat();
        if (object.has("offset")) value.offset = object.get("offset").getAsFloat();
        if (object.has("min")) value.min = object.get("min").getAsFloat();
        if (object.has("max")) value.max = object.get("max").getAsFloat();
        return value;
    }

}
