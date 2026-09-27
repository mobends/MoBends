package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.state.template.SpaceTemplate;

import java.lang.reflect.Type;
import java.util.Map;

/** "PRE" | "POST" | {"default": "PRE", "body": "POST"} */
public class SpaceTemplateSerializer implements JsonDeserializer<SpaceTemplate>
{

    @Override
    public SpaceTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        SpaceTemplate space = new SpaceTemplate();
        if (json.isJsonPrimitive())
        {
            space.defaultSpace = JsonReading.enumValue(Pose.Space.class, json, "A space");
            return space;
        }
        for (Map.Entry<String, JsonElement> entry : JsonReading.object(json, "A space").entrySet())
        {
            Pose.Space value = JsonReading.enumValue(Pose.Space.class, entry.getValue(), "The space of '" + entry.getKey() + "'");
            if ("default".equals(entry.getKey()))
            {
                space.defaultSpace = value;
            }
            else
            {
                space.perBone.put(entry.getKey(), value);
            }
        }
        return space;
    }

}
