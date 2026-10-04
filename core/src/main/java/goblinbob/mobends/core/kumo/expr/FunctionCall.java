package goblinbob.mobends.core.kumo.expr;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import goblinbob.mobends.core.kumo.state.template.FunctionTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.HashMap;
import java.util.Map;

/**
 * A call of a function a scope's {@code @functions} declares, {@code {"animator.wobble": [t, 5]}}:
 * inlined, as if its body were written out at the call site with the arguments in place. The body
 * reads its parameters ({@code arg.t}), the built-ins and the names of the scope declaring it; an
 * argument is compiled where the call is written, at every place the body reads it, so each gets
 * its own memory (an edge trigger in an argument behaves as if written out). Arguments written out
 * (a constant, a string, a choice) are put in the body as they are.
 */
public final class FunctionCall
{

    /** A function, and the place its body is compiled in: the scope declaring it. */
    public static final class Declared
    {
        final String name;
        final FunctionTemplate template;
        final ExpressionScope place;

        public Declared(String name, FunctionTemplate template, ExpressionScope place)
        {
            this.name = name;
            this.template = template;
            this.place = place;
        }
    }

    /** An argument read as an expression: its JSON, and the place it is written in. */
    public static final class Argument
    {
        final JsonElement json;
        final ExpressionScope scope;

        Argument(JsonElement json, ExpressionScope scope)
        {
            this.json = json;
            this.scope = scope;
        }
    }

    private FunctionCall()
    {
    }

    /** Checks a function's body once, where it is declared, with stand-ins for its arguments. */
    public static void check(Declared function) throws MalformedKumoTemplateException
    {
        JsonArray standIns = new JsonArray();
        for (FunctionTemplate.Param param : function.template.params.values())
        {
            switch (param.kind)
            {
                case "boolean": standIns.add(new JsonPrimitive(false)); break;
                case "string": standIns.add(new JsonPrimitive("")); break;
                case "choice": standIns.add(new JsonPrimitive(param.choices.isEmpty() ? "" : param.choices.get(0))); break;
                default: standIns.add(new JsonPrimitive(0)); break;
            }
        }
        try
        {
            body(function, standIns, function.place, standIns);
        }
        catch (MalformedKumoTemplateException e)
        {
            throw new MalformedKumoTemplateException(String.format("In the function '%s': %s", function.name, e.getMessage()));
        }
    }

    static Expression compile(String name, JsonArray arguments, ExpressionScope scope, JsonElement whole) throws MalformedKumoTemplateException
    {
        Declared function = scope.resolveFunction(name);
        return body(function, arguments, scope, whole);
    }

    private static Expression body(Declared function, JsonArray arguments, ExpressionScope callSite, JsonElement whole) throws MalformedKumoTemplateException
    {
        if (callSite.getCalling().contains(function.name))
        {
            throw new MalformedKumoTemplateException(String.format("'%s' calls itself (through %s): a call is its body written out, which would never end.",
                    function.name, String.join(" -> ", callSite.getCalling())));
        }
        Map<String, FunctionTemplate.Param> params = function.template.params;
        if (arguments.size() != params.size())
        {
            throw new MalformedKumoTemplateException(String.format("'%s' takes %d argument%s, not %d: %s", function.name, params.size(), params.size() == 1 ? "" : "s",
                    arguments.size(), Expression.describe(whole)));
        }
        Map<String, Argument> bound = new HashMap<>();
        Map<String, JsonElement> writtenOut = new HashMap<>();
        int i = 0;
        for (Map.Entry<String, FunctionTemplate.Param> entry : params.entrySet())
        {
            JsonElement argument = arguments.get(i++);
            String where = String.format("'%s' argument %d (%s)", function.name, i, entry.getKey());
            FunctionTemplate.Param param = entry.getValue();
            switch (param.kind)
            {
                case "number":
                case "boolean":
                    Expression.Type type = "number".equals(param.kind) ? Expression.Type.NUMBER : Expression.Type.BOOLEAN;
                    Expression checked = Expression.compileAny(argument, callSite);
                    if (checked.getType() != type)
                    {
                        throw new MalformedKumoTemplateException(where + " must be " + type.description + ", got " + checked.getType().description + ": " + Expression.describe(whole));
                    }
                    bound.put(entry.getKey(), new Argument(argument, callSite));
                    break;
                case "constant":
                    if (!argument.isJsonPrimitive() || !argument.getAsJsonPrimitive().isNumber())
                    {
                        throw new MalformedKumoTemplateException(where + " must be a number written out, got " + Expression.describe(argument) + ".");
                    }
                    writtenOut.put(entry.getKey(), argument);
                    break;
                default:
                    if (!argument.isJsonPrimitive() || !argument.getAsJsonPrimitive().isString())
                    {
                        throw new MalformedKumoTemplateException(where + " must be a string, got " + Expression.describe(argument) + ".");
                    }
                    if ("choice".equals(param.kind) && !param.choices.contains(argument.getAsString()))
                    {
                        throw new MalformedKumoTemplateException(where + " must be one of " + String.join(", ", param.choices) + ", got '" + argument.getAsString() + "'.");
                    }
                    writtenOut.put(entry.getKey(), argument);
            }
        }
        JsonElement body = substitute(new JsonParser().parse(function.template.body.toString()), writtenOut);
        return Expression.compileAny(body, function.place.withArguments(bound, function.name, callSite.getCalling()));
    }

    /** The body with the arguments written out in place of their parameters ({@code "arg.item"}). */
    private static JsonElement substitute(JsonElement json, Map<String, JsonElement> writtenOut)
    {
        if (writtenOut.isEmpty() || json == null || json instanceof JsonNull)
        {
            return json;
        }
        if (json.isJsonPrimitive())
        {
            JsonPrimitive primitive = json.getAsJsonPrimitive();
            if (primitive.isString() && primitive.getAsString().startsWith("arg."))
            {
                JsonElement argument = writtenOut.get(primitive.getAsString().substring(4));
                return argument == null ? json : argument;
            }
            return json;
        }
        if (json.isJsonArray())
        {
            JsonArray array = new JsonArray();
            for (JsonElement element : json.getAsJsonArray())
            {
                array.add(substitute(element, writtenOut));
            }
            return array;
        }
        JsonObject object = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet())
        {
            object.add(entry.getKey(), substitute(entry.getValue(), writtenOut));
        }
        return object;
    }

}
