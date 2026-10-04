package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.bind.IVectorSink;
import goblinbob.mobends.core.kumo.driver.DriverRegistry;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.state.template.DampingTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.ClipItemTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.PoseItemTemplate;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A pose item: an object with exactly one key that doesn't start with {@code @}, naming it
 * ({@code core:clip} or a driver), whose value is the item's own fields; the other keys are
 * modifiers ({@code @when}, {@code @space}, ...).
 */
public class PoseItemSerializer implements JsonDeserializer<PoseItemTemplate>
{

    public static final String CLIP = "core:clip";

    private static final Set<String> MODIFIERS = new HashSet<>(Arrays.asList(
            "@when", "@space", "@damping", "@snap", "@mirror", "@swapSides", "@vectorModes", JsonReading.COMMENT));
    private static final Type VECTOR_MODES = new TypeToken<Map<String, IVectorSink.Mode>>() {}.getType();

    @Override
    public PoseItemTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = JsonReading.object(json, "A pose item");
        String kind = null;
        for (Map.Entry<String, JsonElement> entry : object.entrySet())
        {
            String key = entry.getKey();
            if (key.startsWith("@"))
            {
                if (!MODIFIERS.contains(key))
                {
                    throw new JsonParseException(String.format("A pose item has an unknown modifier \"%s\" (it takes \"@when\", \"@space\", \"@damping\", "
                            + "\"@snap\", \"@mirror\", \"@swapSides\" and \"@vectorModes\").", key));
                }
            }
            else if (kind != null)
            {
                throw new JsonParseException(String.format("A pose item has exactly one key that doesn't start with @, naming it: got \"%s\" and \"%s\".", kind, key));
            }
            else
            {
                kind = key;
            }
        }
        if (kind == null)
        {
            throw new JsonParseException("A pose item needs a key naming it (\"core:clip\" or a driver, such as \"core:axis_rotate\").");
        }

        String what = String.format("The pose item \"%s\"", kind);
        JsonObject content = JsonReading.object(object.get(kind), what);
        PoseItemTemplate item;
        if (CLIP.equals(kind))
        {
            item = read(content, ClipItemTemplate.class, what);
        }
        else
        {
            Class<? extends DriverItemTemplate> templateType = DriverRegistry.INSTANCE.getTemplateClass(kind);
            if (templateType == null)
            {
                throw new JsonParseException(String.format("Unknown pose item: \"%s\" (\"core:clip\" or a driver).", kind));
            }
            DriverItemTemplate driver = read(content, templateType, what);
            driver.driver = kind;
            item = driver;
        }

        JsonElement when = object.get("@when");
        if (when != null && !when.isJsonNull())
        {
            item.when = new ExpressionTemplate(when);
        }
        if (object.has("@space"))
        {
            item.space = JsonReading.enumValue(Pose.Space.class, object.get("@space"), what + "'s \"@space\"");
        }
        if (object.has("@damping"))
        {
            item.damping = KumoSerializer.INSTANCE.leafGson.fromJson(object.get("@damping"), DampingTemplate.class);
        }
        if (object.has("@vectorModes"))
        {
            item.vectorModes = KumoSerializer.INSTANCE.leafGson.fromJson(object.get("@vectorModes"), VECTOR_MODES);
        }
        item.snap = flag(object, "@snap", what);
        item.mirror = flag(object, "@mirror", what);
        item.swapSides = flag(object, "@swapSides", what);
        return item;
    }

    /** Reads a construct's own fields into {@code type}: any other key is an error. */
    private static <T extends PoseItemTemplate> T read(JsonObject content, Class<T> type, String what)
    {
        Map<String, String> keys = JsonReading.ownFields(type, type == ClipItemTemplate.class ? PoseItemTemplate.class : DriverItemTemplate.class);
        return KumoSerializer.INSTANCE.leafGson.fromJson(JsonReading.fields(content, what, keys), type);
    }

    private static boolean flag(JsonObject object, String key, String what)
    {
        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull())
        {
            return false;
        }
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean())
        {
            throw new JsonParseException(String.format("%s's \"%s\" has to be true or false.", what, key));
        }
        return value.getAsBoolean();
    }

}
