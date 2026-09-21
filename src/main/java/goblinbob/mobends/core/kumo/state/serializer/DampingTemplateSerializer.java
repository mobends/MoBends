package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.state.template.DampingTemplate;

import java.lang.reflect.Type;
import java.util.Map;

/** {"default": 0.3, "rightArm": 0.8, "root": [0.3, 0.6, 0.3]} */
public class DampingTemplateSerializer implements JsonDeserializer<DampingTemplate>
{

    @Override
    public DampingTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        DampingTemplate damping = new DampingTemplate();
        if (json.isJsonPrimitive())
        {
            damping.entries.put(DampingTemplate.DEFAULT, new float[] { json.getAsFloat() });
            return damping;
        }
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet())
        {
            JsonElement value = entry.getValue();
            if (value.isJsonArray())
            {
                JsonArray array = value.getAsJsonArray();
                float[] values = new float[array.size()];
                for (int i = 0; i < values.length; i++)
                {
                    values[i] = array.get(i).getAsFloat();
                }
                damping.entries.put(entry.getKey(), values);
            }
            else
            {
                damping.entries.put(entry.getKey(), new float[] { value.getAsFloat() });
            }
        }
        return damping;
    }

}
