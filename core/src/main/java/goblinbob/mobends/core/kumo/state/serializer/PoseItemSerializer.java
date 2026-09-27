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
        JsonObject object = json.getAsJsonObject();
        if (object.has("driver"))
        {
            String key = object.get("driver").getAsString();
            Type templateType = DriverRegistry.INSTANCE.getTemplateClass(key);
            if (templateType == null)
            {
                throw new JsonParseException(String.format("Unknown driver: '%s'", key));
            }
            return KumoSerializer.INSTANCE.keyframeNodeGson.fromJson(json, templateType);
        }
        return KumoSerializer.INSTANCE.keyframeNodeGson.fromJson(json, ClipItemTemplate.class);
    }

}
