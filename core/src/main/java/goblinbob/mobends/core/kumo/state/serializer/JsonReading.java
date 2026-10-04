package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Shared checks of the animator deserializers: malformed input always ends in a {@link JsonParseException}. */
final class JsonReading
{

    private JsonReading() {}

    /** {@code json} as a list, or an error naming {@code what}. */
    static com.google.gson.JsonArray array(JsonElement json, String what)
    {
        if (json == null || !json.isJsonArray())
        {
            throw new JsonParseException(String.format("%s has to be a list.", what));
        }
        return json.getAsJsonArray();
    }

    static JsonObject object(JsonElement json, String what)
    {
        if (json == null || !json.isJsonObject())
        {
            throw new JsonParseException(String.format("%s has to be an object.", what));
        }
        return json.getAsJsonObject();
    }

    static String string(JsonElement json, String what)
    {
        if (json == null || !json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString())
        {
            throw new JsonParseException(String.format("%s has to be a string.", what));
        }
        return json.getAsString();
    }

    static float number(JsonElement json, String what)
    {
        if (json == null || !json.isJsonPrimitive() || !json.getAsJsonPrimitive().isNumber())
        {
            throw new JsonParseException(String.format("%s has to be a number.", what));
        }
        return json.getAsFloat();
    }

    /** A key every object of every Mo' Bends file may have; the engine ignores it. */
    static final String COMMENT = "@comment";

    /**
     * A copy of {@code object} for Gson to read into a template: the keys in {@code keys} renamed to
     * the template's field names, {@code @comment} left out. Any other key is an error.
     *
     * @param keys the keys the object may have, each mapped to its field name
     */
    static JsonObject fields(JsonObject object, String what, Map<String, String> keys)
    {
        JsonObject copy = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet())
        {
            String key = entry.getKey();
            if (COMMENT.equals(key))
            {
                continue;
            }
            String field = keys.get(key);
            if (field == null)
            {
                throw new JsonParseException(String.format("%s has an unknown key \"%s\" (it takes %s).", what, key, describe(keys.keySet())));
            }
            copy.add(field, entry.getValue());
        }
        return copy;
    }

    /** {@code names} as a map of each name to itself, for {@link #fields}. */
    static Map<String, String> same(String... names)
    {
        Map<String, String> keys = new LinkedHashMap<>();
        for (String name : names)
        {
            keys.put(name, name);
        }
        return keys;
    }

    /** {@code keys} plus {@code key} read into {@code field}. */
    static Map<String, String> with(Map<String, String> keys, String key, String field)
    {
        Map<String, String> copy = new LinkedHashMap<>(keys);
        copy.put(key, field);
        return copy;
    }

    /**
     * The JSON names of the fields Gson reads into {@code type}, from {@code type} up to, not
     * including, {@code stop}: what a construct's own object may contain.
     */
    static Map<String, String> ownFields(Class<?> type, Class<?> stop)
    {
        Map<String, String> keys = new LinkedHashMap<>();
        for (Class<?> c = type; c != null && c != stop && c != Object.class; c = c.getSuperclass())
        {
            for (Field field : c.getDeclaredFields())
            {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic())
                {
                    continue;
                }
                SerializedName serialized = field.getAnnotation(SerializedName.class);
                String name = serialized == null ? field.getName() : serialized.value();
                keys.put(name, name);
            }
        }
        return keys;
    }

    private static String describe(Collection<String> keys)
    {
        if (keys.isEmpty())
        {
            return "none but \"@comment\"";
        }
        StringBuilder text = new StringBuilder();
        for (String key : keys)
        {
            text.append(text.length() == 0 ? "" : ", ").append('"').append(key).append('"');
        }
        return text.toString();
    }

    /** Throws unless {@code key}, in a map of bones, is a bone name or {@code @default}. */
    static void requireBoneOrDefault(String key, String what)
    {
        if (key.startsWith("@") && !"@default".equals(key))
        {
            throw new JsonParseException(String.format("%s: unknown key \"%s\" (bones, \"@default\" for the others, and \"@comment\").", what, key));
        }
    }

    /** The constant of {@code type} named by {@code json}, written in lower case (see {@link LowerCaseEnums}). */
    static <E extends Enum<E>> E enumValue(Class<E> type, JsonElement json, String what)
    {
        return LowerCaseEnums.valueOf(type, string(json, what), what);
    }

}
