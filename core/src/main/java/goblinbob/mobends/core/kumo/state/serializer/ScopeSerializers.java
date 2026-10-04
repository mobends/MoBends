package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import goblinbob.mobends.core.kumo.state.template.DefinitionTemplate;
import goblinbob.mobends.core.kumo.state.template.OnTemplate;
import goblinbob.mobends.core.kumo.state.template.StatementTemplate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Readers of what a scope has: its definitions ({@code @define}), its statement lists ({@code @on}) and their statements. */
public final class ScopeSerializers
{

    private ScopeSerializers()
    {
    }

    /** {@code {"live": <expression>}}: exactly one key, the definition's kind. */
    public static class Definition implements JsonDeserializer<DefinitionTemplate>
    {
        @Override
        public DefinitionTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject object = JsonReading.object(json, "A definition");
            DefinitionTemplate.Kind kind = null;
            JsonElement expression = null;
            for (Map.Entry<String, JsonElement> entry : object.entrySet())
            {
                if (JsonReading.COMMENT.equals(entry.getKey()))
                {
                    continue;
                }
                DefinitionTemplate.Kind found = null;
                for (DefinitionTemplate.Kind candidate : DefinitionTemplate.Kind.values())
                {
                    if (candidate.key.equals(entry.getKey())) found = candidate;
                }
                if (found == null)
                {
                    throw new JsonParseException(String.format("A definition has an unknown key \"%s\": it is {\"constant\": ...}, {\"state\": ...} or {\"live\": ...}.", entry.getKey()));
                }
                if (kind != null)
                {
                    throw new JsonParseException(String.format("A definition has exactly one kind: got \"%s\" and \"%s\".", kind.key, found.key));
                }
                kind = found;
                expression = entry.getValue();
            }
            if (kind == null)
            {
                throw new JsonParseException("A definition needs its kind: {\"constant\": ...}, {\"state\": ...} or {\"live\": ...}.");
            }
            return new DefinitionTemplate(kind, expression);
        }
    }

    /** {@code {"enter": [...], "update": [...], "exit": [...]}}. */
    public static class On implements JsonDeserializer<OnTemplate>
    {
        @Override
        public OnTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject object = JsonReading.fields(JsonReading.object(json, "\"@on\""), "\"@on\"", JsonReading.same("enter", "update", "exit"));
            return new OnTemplate(list(object.get("enter"), "\"@on\" \"enter\"", context),
                                  list(object.get("update"), "\"@on\" \"update\"", context),
                                  list(object.get("exit"), "\"@on\" \"exit\"", context));
        }
    }

    /** A list of statements, or an empty list for null. */
    public static List<StatementTemplate> list(JsonElement json, String what, JsonDeserializationContext context)
    {
        if (json == null || json.isJsonNull())
        {
            return Collections.emptyList();
        }
        if (!json.isJsonArray())
        {
            throw new JsonParseException(String.format("%s has to be a list of statements.", what));
        }
        List<StatementTemplate> statements = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray())
        {
            statements.add(context.deserialize(element, StatementTemplate.class));
        }
        return statements;
    }

    /** {@code {"@when": <condition>, "set": ["layer.combo", <value>]}}. */
    public static class Statement implements JsonDeserializer<StatementTemplate>
    {
        @Override
        public StatementTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject object = JsonReading.fields(JsonReading.object(json, "A statement"), "A statement", JsonReading.with(JsonReading.same("set"), "@when", "when"));
            JsonElement set = object.get("set");
            if (set == null)
            {
                throw new JsonParseException("A statement needs its key: {\"set\": [\"layer.combo\", <value>]}.");
            }
            if (!set.isJsonArray() || set.getAsJsonArray().size() != 2)
            {
                throw new JsonParseException("\"set\" takes two arguments, the state and its new value: {\"set\": [\"layer.combo\", 0]}.");
            }
            JsonArray arguments = set.getAsJsonArray();
            String target = JsonReading.string(arguments.get(0), "\"set\"'s first argument (the state it sets)");
            JsonElement when = object.get("when");
            return new StatementTemplate(target, arguments.get(1), when == null || when.isJsonNull() ? null : when);
        }
    }

}
