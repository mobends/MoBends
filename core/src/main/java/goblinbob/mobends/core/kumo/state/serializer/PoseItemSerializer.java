package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.driver.DriverRegistry;
import goblinbob.mobends.core.kumo.state.template.pose.ClipItemTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.PoseItemTemplate;

import java.lang.reflect.Type;

/** A pose item is a clip ({@code animationKey}) or a driver ({@code driver}). */
public class PoseItemSerializer implements JsonDeserializer<PoseItemTemplate>
{

    @Override
    public PoseItemTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = JsonReading.object(json, "A pose item");
        if (object.has("driver"))
        {
            String key = JsonReading.string(object.get("driver"), "A pose item's \"driver\"");
            Type templateType = DriverRegistry.INSTANCE.getTemplateClass(key);
            if (templateType == null)
            {
                throw new JsonParseException(String.format("Unknown driver: '%s'.", key));
            }
            return KumoSerializer.INSTANCE.leafGson.fromJson(json, templateType);
        }
        if (!object.has("animationKey"))
        {
            throw new JsonParseException("A pose item has neither an \"animationKey\" nor a \"driver\".");
        }
        return KumoSerializer.INSTANCE.leafGson.fromJson(json, ClipItemTemplate.class);
    }

}
