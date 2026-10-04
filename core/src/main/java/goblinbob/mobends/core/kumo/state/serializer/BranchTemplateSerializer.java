package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import goblinbob.mobends.core.kumo.state.template.BranchTemplate;
import goblinbob.mobends.core.kumo.state.template.ConnectionTemplate;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

/** A selector branch: {@code then} is a node or machine name, or a list of branches. */
public class BranchTemplateSerializer implements JsonDeserializer<BranchTemplate>
{

    private static final Type SET_TYPE = new TypeToken<Map<String, Float>>() {}.getType();

    @Override
    public BranchTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = JsonReading.fields(JsonReading.object(json, "A selector branch"), "A selector branch",
                JsonReading.same("when", "then", "transitionDuration", "transitionEasing", "set"));
        BranchTemplate branch = new BranchTemplate();

        JsonElement when = object.get("when");
        if (when != null && !when.isJsonNull())
        {
            branch.when = new ExpressionTemplate(when);
        }

        JsonElement then = object.get("then");
        if (then == null || then.isJsonNull())
        {
            throw new JsonParseException("A selector branch has no \"then\".");
        }
        if (then.isJsonArray())
        {
            branch.branches = new ArrayList<>();
            for (JsonElement element : then.getAsJsonArray())
            {
                branch.branches.add(deserialize(element, typeOfT, context));
            }
        }
        else
        {
            branch.target = JsonReading.string(then, "A selector branch's \"then\" (a name or a list of branches)");
        }

        JsonElement duration = object.get("transitionDuration");
        if (duration != null && !duration.isJsonNull())
        {
            branch.transitionDuration = JsonReading.number(duration, "A selector branch's \"transitionDuration\"");
        }
        JsonElement easing = object.get("transitionEasing");
        if (easing != null && !easing.isJsonNull())
        {
            branch.transitionEasing = JsonReading.enumValue(ConnectionTemplate.Easing.class, easing, "A selector branch's \"transitionEasing\"");
        }
        JsonElement set = object.get("set");
        if (set != null && !set.isJsonNull())
        {
            branch.set = context.deserialize(set, SET_TYPE);
        }
        return branch;
    }

}
