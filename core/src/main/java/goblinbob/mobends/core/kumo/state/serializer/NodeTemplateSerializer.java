package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.node.NodeRegistry;
import goblinbob.mobends.core.kumo.state.template.NodeTemplate;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * A node: an object with exactly one key that doesn't start with {@code @}, its type
 * ({@code core:pose}, {@code core:fallthrough}, {@code core:vanilla} or an addon's), whose value is
 * what the type takes; the scope's own keys start with {@code @} ({@code @connections}, ...).
 */
public class NodeTemplateSerializer implements JsonDeserializer<NodeTemplate>
{

    /** The keys every node has whatever its type, and the template fields they are read into. */
    private static final Map<String, String> SCOPE_KEYS = JsonReading.with(JsonReading.with(JsonReading.with(JsonReading.with(
            JsonReading.same(), "@connections", "connections"), "@define", "define"), "@on", "on"), "@tags", "tags");

    @Override
    public NodeTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = JsonReading.object(json, "A node");
        String type = null;
        JsonObject scope = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet())
        {
            String key = entry.getKey();
            if (key.startsWith("@"))
            {
                scope.add(key, entry.getValue());
            }
            else if (type != null)
            {
                throw new JsonParseException(String.format("A node has exactly one key that doesn't start with @, its type: got \"%s\" and \"%s\".", type, key));
            }
            else
            {
                type = key;
            }
        }
        if (type == null)
        {
            throw new JsonParseException("A node needs a key naming its type, such as \"core:pose\": {\"core:pose\": {\"pose\": [...]}}.");
        }

        Class<? extends NodeTemplate> templateType = NodeRegistry.INSTANCE.getTemplateClass(type);
        if (templateType == null)
        {
            throw new JsonParseException(String.format("Unknown node type: \"%s\".", type));
        }
        String what = String.format("The node type \"%s\"", type);
        Map<String, String> own = JsonReading.ownFields(templateType, Object.class);
        own.keySet().removeAll(NodeTemplate.SCOPE_FIELDS);
        JsonObject fields = JsonReading.fields(JsonReading.object(object.get(type), what), what, own);
        for (Map.Entry<String, JsonElement> entry : JsonReading.fields(scope, "A node", SCOPE_KEYS).entrySet())
        {
            fields.add(entry.getKey(), entry.getValue());
        }
        NodeTemplate template = KumoSerializer.INSTANCE.leafGson.fromJson(fields, templateType);
        template.setType(type);
        return template;
    }

}
