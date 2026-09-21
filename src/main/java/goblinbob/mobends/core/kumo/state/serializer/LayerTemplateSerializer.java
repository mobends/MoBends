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
 * Picks the layer template class by {@code type}, and accepts both node forms:
 * a JSON array (legacy, index-addressed) or a JSON object keyed by node name (format 2).
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
            List<String> names = null;
            JsonElement nodes = copy.get("nodes");
            if (nodes != null && nodes.isJsonObject())
            {
                names = new ArrayList<>();
                JsonArray array = new JsonArray();
                for (Map.Entry<String, JsonElement> entry : nodes.getAsJsonObject().entrySet())
                {
                    names.add(entry.getKey());
                    array.add(entry.getValue());
                }
                copy.add("nodes", array);
            }

            JsonElement entry = copy.get("entryNode");
            String entryName = null;
            if (entry != null && entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString())
            {
                entryName = entry.getAsString();
                copy.remove("entryNode");
            }

            KeyframeLayerTemplate layer = KumoSerializer.INSTANCE.layerGson.fromJson(copy, KeyframeLayerTemplate.class);
            if (names != null && layer.nodes != null)
            {
                for (int i = 0; i < names.size() && i < layer.nodes.size(); i++)
                {
                    KeyframeNodeTemplate node = layer.nodes.get(i);
                    if (node != null && node.name == null)
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
