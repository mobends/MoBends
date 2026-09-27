package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.LayerType;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeLayerTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Picks the layer template class by {@code type}. A keyframe layer's {@code nodes} is an object
 * keyed by node name, and its {@code entryNode} is one of those names.
 */
public class LayerTemplateSerializer implements JsonSerializer<LayerTemplate>, JsonDeserializer<LayerTemplate>
{

    @Override
    public JsonElement serialize(LayerTemplate src, Type typeOfSrc, JsonSerializationContext context)
    {
        return KumoSerializer.INSTANCE.layerGson.toJsonTree(src, src.getLayerType().getTemplateType());
    }

    @Override
    public LayerTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = json.getAsJsonObject();
        LayerType type = LayerType.KEYFRAME;
        if (object.has("type"))
        {
            type = LayerType.valueOf(object.get("type").getAsString());
        }

        if (type == LayerType.KEYFRAME)
        {
            // Shallow copy: only the top-level entries are rewritten below, and
            // JsonObject.deepCopy() is not public in the Gson shipped with 1.12.2.
            JsonObject copy = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet())
            {
                copy.add(entry.getKey(), entry.getValue());
            }
            List<String> names = new ArrayList<>();
            JsonElement nodes = copy.get("nodes");
            if (nodes != null && !nodes.isJsonObject())
            {
                throw new JsonParseException("A keyframe layer's \"nodes\" has to be an object keyed by node name.");
            }
            if (nodes != null)
            {
                JsonArray array = new JsonArray();
                for (Map.Entry<String, JsonElement> entry : nodes.getAsJsonObject().entrySet())
                {
                    names.add(entry.getKey());
                    array.add(entry.getValue());
                }
                copy.add("nodes", array);
            }

            JsonElement entry = copy.get("entryNode");
            if (entry == null || !entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString())
            {
                throw new JsonParseException("A keyframe layer's \"entryNode\" has to be the name of one of its nodes.");
            }
            String entryName = entry.getAsString();
            copy.remove("entryNode");

            KeyframeLayerTemplate layer = KumoSerializer.INSTANCE.layerGson.fromJson(copy, KeyframeLayerTemplate.class);
            if (layer.nodes != null)
            {
                for (int i = 0; i < names.size() && i < layer.nodes.size(); i++)
                {
                    KeyframeNodeTemplate node = layer.nodes.get(i);
                    if (node != null)
                    {
                        node.name = names.get(i);
                    }
                }
            }
            layer.entryNodeName = entryName;
            return layer;
        }

        return KumoSerializer.INSTANCE.layerGson.fromJson(json, type.getTemplateType());
    }

}
