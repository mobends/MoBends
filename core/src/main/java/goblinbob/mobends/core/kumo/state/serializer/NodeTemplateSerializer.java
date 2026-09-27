package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.node.NodeRegistry;
import goblinbob.mobends.core.kumo.state.template.NodeTemplate;

import java.lang.reflect.Type;

/** Picks the node template class by {@code type} ({@code core:pose} when left out). */
public class NodeTemplateSerializer implements JsonDeserializer<NodeTemplate>
{

    @Override
    public NodeTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = JsonReading.object(json, "A node");
        String type = object.has("type") ? JsonReading.string(object.get("type"), "A node's \"type\"") : "core:pose";

        Type templateType = NodeRegistry.INSTANCE.getTemplateClass(type);
        if (templateType == null)
        {
            throw new JsonParseException(String.format("Unknown node type: '%s'.", type));
        }

        NodeTemplate template = KumoSerializer.INSTANCE.leafGson.fromJson(json, templateType);
        template.setType(type);
        return template;
    }

}
