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

    /** The keys of a machine, and the template fields they are read into. */
    private static final Map<String, String> MACHINE_KEYS = JsonReading.with(JsonReading.with(JsonReading.with(
            JsonReading.same("nodes", "machines", "select", "defaultOnEntry"), "@connections", "connections"), "@define", "define"), "@on", "on");
    /** A layer's: a machine's, and the layer's own. */
    private static final Map<String, String> LAYER_KEYS = JsonReading.with(withAll(MACHINE_KEYS,
            "mode", "additiveSpace", "damping", "mask", "mirror"), "@when", "when");

    private static Map<String, String> withAll(Map<String, String> keys, String... names)
    {
        Map<String, String> copy = keys;
        for (String name : names)
        {
            copy = JsonReading.with(copy, name, name);
        }
        return copy;
    }

    @Override
    public LayerTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return readMachine(json, LayerTemplate.class, "A layer", LAYER_KEYS);
    }

    private static <T extends MachineTemplate> T readMachine(JsonElement json, Class<T> type, String what, Map<String, String> keys)
    {
        // A copy with the keys renamed to the template's fields (JsonObject.deepCopy() is not public
        // in the Gson shipped with 1.12.2), without the entries read here.
        JsonObject copy = JsonReading.fields(JsonReading.object(json, what), what, keys);
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
                MachineTemplate nested = readMachine(entry.getValue(), MachineTemplate.class, String.format("The machine '%s'", entry.getKey()), MACHINE_KEYS);
                nested.name = entry.getKey();
                machine.machines.add(nested);
            }
        }
        return machine;
    }

}
