package goblinbob.mobends.core.definition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A model definition that {@code extends} another is that one with its own on top, merged as JSON
 * before it is read:
 * <ul>
 * <li>{@code bones}: a bone of the same name replaces the other's where it stands; the rest come
 * after;</li>
 * <li>{@code @define}: both declare theirs, and declaring a name the other does is an error (as
 * for animators);</li>
 * <li>{@code @on}: each list runs the other's statements, then its own;</li>
 * <li>{@code components}, {@code layers}, {@code childBones}, {@code smoothness}, {@code renderer}:
 * key by key, its own winning;</li>
 * <li>anything else: its own, if it has it.</li>
 * </ul>
 */
public final class DefinitionMerge
{

    private static final List<String> BY_KEY = Arrays.asList("components", "layers", "childBones", "smoothness", "renderer");

    private DefinitionMerge()
    {
    }

    public static JsonObject merge(JsonObject parent, JsonObject child) throws MalformedKumoTemplateException
    {
        JsonObject merged = copy(parent).getAsJsonObject();
        merged.remove("extends");
        for (Map.Entry<String, JsonElement> entry : child.entrySet())
        {
            String key = entry.getKey();
            JsonElement own = entry.getValue();
            JsonElement inherited = merged.get(key);
            if (key.equals("extends") || inherited == null)
            {
                if (!key.equals("extends")) merged.add(key, copy(own));
                continue;
            }
            if (key.equals("bones") && own.isJsonArray() && inherited.isJsonArray())
            {
                merged.add(key, bones(inherited.getAsJsonArray(), own.getAsJsonArray()));
            }
            else if (key.equals("@define") && own.isJsonObject() && inherited.isJsonObject())
            {
                JsonObject define = inherited.getAsJsonObject();
                for (Map.Entry<String, JsonElement> definition : own.getAsJsonObject().entrySet())
                {
                    if (define.has(definition.getKey()))
                    {
                        throw new MalformedKumoTemplateException("'@define' declares '" + definition.getKey() + "', which the model definition it extends declares.");
                    }
                    define.add(definition.getKey(), copy(definition.getValue()));
                }
            }
            else if (key.equals("@on") && own.isJsonObject() && inherited.isJsonObject())
            {
                JsonObject on = inherited.getAsJsonObject();
                for (Map.Entry<String, JsonElement> list : own.getAsJsonObject().entrySet())
                {
                    JsonElement before = on.get(list.getKey());
                    if (before != null && before.isJsonArray() && list.getValue().isJsonArray())
                    {
                        before.getAsJsonArray().addAll(copy(list.getValue()).getAsJsonArray());
                    }
                    else
                    {
                        on.add(list.getKey(), copy(list.getValue()));
                    }
                }
            }
            else if (BY_KEY.contains(key) && own.isJsonObject() && inherited.isJsonObject())
            {
                for (Map.Entry<String, JsonElement> item : own.getAsJsonObject().entrySet())
                {
                    inherited.getAsJsonObject().add(item.getKey(), copy(item.getValue()));
                }
            }
            else
            {
                merged.add(key, copy(own));
            }
        }
        return merged;
    }

    /** A copy (Minecraft's Gson has no public deepCopy). */
    private static JsonElement copy(JsonElement json)
    {
        return new JsonParser().parse(json.toString());
    }

    private static JsonArray bones(JsonArray inherited, JsonArray own)
    {
        JsonArray merged = copy(inherited).getAsJsonArray();
        for (JsonElement bone : own)
        {
            int at = indexOf(merged, name(bone));
            if (at >= 0)
            {
                merged.set(at, copy(bone));
            }
            else
            {
                merged.add(copy(bone));
            }
        }
        return merged;
    }

    private static int indexOf(JsonArray bones, String name)
    {
        for (int i = 0; name != null && i < bones.size(); i++)
        {
            if (name.equals(name(bones.get(i)))) return i;
        }
        return -1;
    }

    private static String name(JsonElement bone)
    {
        return bone.isJsonObject() && bone.getAsJsonObject().has("name") ? bone.getAsJsonObject().get("name").getAsString() : null;
    }

}
