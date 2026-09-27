package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.NodeTemplate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** A layer's {@code nodes} is an object keyed by node name, and its {@code entryNode} is one of those names. */
public class LayerTemplateSerializer implements JsonDeserializer<LayerTemplate>
{

    @Override
    public LayerTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = JsonReading.object(json, "A layer");

        // Shallow copy: only the top-level entries are rewritten below, and
        // JsonObject.deepCopy() is not public in the Gson shipped with 1.12.2.
        JsonObject copy = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet())
        {
            copy.add(entry.getKey(), entry.getValue());
        }
        List<String> names = new ArrayList<>();
        JsonElement nodes = copy.get("nodes");
        if (nodes != null)
        {
            JsonArray array = new JsonArray();
            for (Map.Entry<String, JsonElement> entry : JsonReading.object(nodes, "A layer's \"nodes\"").entrySet())
            {
                names.add(entry.getKey());
                array.add(entry.getValue());
            }
            copy.add("nodes", array);
        }

        String entryName = JsonReading.string(copy.remove("entryNode"), "A layer's \"entryNode\"");

        LayerTemplate layer = KumoSerializer.INSTANCE.layerGson.fromJson(copy, LayerTemplate.class);
        if (layer.nodes != null)
        {
            for (int i = 0; i < names.size() && i < layer.nodes.size(); i++)
            {
                NodeTemplate node = layer.nodes.get(i);
                if (node == null)
                {
                    throw new JsonParseException(String.format("The node '%s' is null.", names.get(i)));
                }
                node.name = names.get(i);
            }
        }
        layer.entryNodeName = entryName;
        return layer;
    }

}
