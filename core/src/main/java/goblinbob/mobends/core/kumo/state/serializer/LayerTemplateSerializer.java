package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MachineTemplate;
import goblinbob.mobends.core.kumo.state.template.NodeTemplate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

/**
 * Reads a layer, the outermost machine: in it and in every machine inside it, {@code nodes} and
 * {@code machines} are objects keyed by name, and {@code defaultOnEntry} is one of those names.
 */
public class LayerTemplateSerializer implements JsonDeserializer<LayerTemplate>
{

    @Override
    public LayerTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return readMachine(json, LayerTemplate.class, "A layer");
    }

    private static <T extends MachineTemplate> T readMachine(JsonElement json, Class<T> type, String what)
    {
        JsonObject object = JsonReading.object(json, what);

        // Shallow copy without the entries read here: JsonObject.deepCopy() is not public in the
        // Gson shipped with 1.12.2.
        JsonObject copy = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet())
        {
            copy.add(entry.getKey(), entry.getValue());
        }
        JsonElement nodes = copy.remove("nodes");
        JsonElement machines = copy.remove("machines");
        JsonElement defaultOnEntry = copy.remove("defaultOnEntry");

        T machine = KumoSerializer.INSTANCE.leafGson.fromJson(copy, type);
        if (defaultOnEntry != null && !defaultOnEntry.isJsonNull())
        {
            machine.defaultOnEntry = JsonReading.string(defaultOnEntry, what + "'s \"defaultOnEntry\"");
        }
        if (nodes != null && !nodes.isJsonNull())
        {
            machine.nodes = new ArrayList<>();
            for (Map.Entry<String, JsonElement> entry : JsonReading.object(nodes, what + "'s \"nodes\"").entrySet())
            {
                NodeTemplate node = KumoSerializer.INSTANCE.layerGson.fromJson(entry.getValue(), NodeTemplate.class);
                if (node == null)
                {
                    throw new JsonParseException(String.format("The node '%s' is null.", entry.getKey()));
                }
                node.name = entry.getKey();
                machine.nodes.add(node);
            }
        }
        if (machines != null && !machines.isJsonNull())
        {
            machine.machines = new ArrayList<>();
            for (Map.Entry<String, JsonElement> entry : JsonReading.object(machines, what + "'s \"machines\"").entrySet())
            {
                MachineTemplate nested = readMachine(entry.getValue(), MachineTemplate.class, String.format("The machine '%s'", entry.getKey()));
                nested.name = entry.getKey();
                machine.machines.add(nested);
            }
        }
        return machine;
    }

}
