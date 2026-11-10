package goblinbob.mobends.core.definition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.VariableTable;
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
 * <li>{@code components}, {@code layers}, {@code childBones}, {@code smoothing}, {@code renderer}, {@code options}:
 * key by key, its own winning;</li>
 * <li>anything else: its own, if it has it.</li>
 * </ul>
 * Its {@code @variants} are merged the same way, after it (see {@link #applyVariants}).
 */
public final class DefinitionMerge
{

    private static final List<String> BY_KEY = Arrays.asList("components", "layers", "childBones", "smoothing", "renderer", "options");

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

    /**
     * {@code json} with its {@code @variants} merged into it, in order, each whose {@code @when}
     * holds, as {@link #merge} merges a definition into the one it extends:
     * {@code "@variants": [{"@when": {"core:minecraft_version": [">", "1.12.2"]}, "childBones": {...}}]}.
     * A condition has to be constant, known when the definition loads (only constant operations,
     * such as {@code core:minecraft_version}); a variant can't say what the definition
     * {@code extends}, nor have variants of its own.
     */
    public static JsonObject applyVariants(JsonObject json) throws MalformedKumoTemplateException
    {
        JsonElement variants = json.get("@variants");
        if (variants == null)
        {
            return json;
        }
        JsonObject merged = copy(json).getAsJsonObject();
        merged.remove("@variants");
        if (!variants.isJsonArray())
        {
            throw new MalformedKumoTemplateException("'@variants' is a list of objects, each with an \"@when\" and the fields it merges in.");
        }
        int index = 0;
        for (JsonElement variant : variants.getAsJsonArray())
        {
            index++;
            String where = "'@variants' entry " + index;
            if (!variant.isJsonObject())
            {
                throw new MalformedKumoTemplateException(where + " is not an object (it has an \"@when\" and the fields it merges in).");
            }
            JsonObject fields = copy(variant).getAsJsonObject();
            JsonElement when = fields.remove("@when");
            fields.remove("@comment");
            if (when == null)
            {
                throw new MalformedKumoTemplateException(where + " needs an \"@when\": the condition under which it is merged in.");
            }
            for (String key : new String[]{"extends", "@variants", "formatVersion"})
            {
                if (fields.has(key))
                {
                    throw new MalformedKumoTemplateException(where + " can't set '" + key + "'.");
                }
            }
            Expression condition;
            try
            {
                condition = Expression.compile(when, ExpressionScope.root(new VariableTable()), Expression.Type.BOOLEAN);
            }
            catch (MalformedKumoTemplateException e)
            {
                throw new MalformedKumoTemplateException(where + ": " + e.getMessage());
            }
            if (!condition.isConstant())
            {
                throw new MalformedKumoTemplateException(where + ": its \"@when\" has to be known when the definition loads (only constant operations, such as core:minecraft_version), got "
                        + when + ".");
            }
            if (condition.test(null))
            {
                merged = merge(merged, fields);
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
