package goblinbob.mobends.core.kumo.expr;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.VariableTable;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * A number computed every frame: a tree of operations over constants and variables, compiled once
 * from its JSON form ({@link ExpressionTemplate}). Arithmetic is in {@code float}.
 */
public abstract class Expression
{

    public static final Expression ZERO = constant(0F);
    public static final Expression ONE = constant(1F);

    /** Built in, unless a named expression takes the name: ticks since the current node started. */
    public static final String ELAPSED_NAME = "elapsed";
    public static final Expression ELAPSED = new Elapsed();

    /** Evaluates the expression; variables resolve through the context's scopes (node, layer, subject). */
    public abstract float get(ITriggerConditionContext context);

    public static Expression constant(float value)
    {
        return new Constant(value);
    }

    /** Compiles {@code template} against {@code scope}, or returns {@code fallback} if there is none. */
    public static Expression compile(@Nullable ExpressionTemplate template, ExpressionScope scope, Expression fallback) throws MalformedKumoTemplateException
    {
        return template == null ? fallback : compile(template.json, scope);
    }

    public static Expression compile(JsonElement json, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        if (json == null || json.isJsonNull())
        {
            throw new MalformedKumoTemplateException("An expression can't be null.");
        }
        if (json.isJsonPrimitive())
        {
            JsonPrimitive primitive = json.getAsJsonPrimitive();
            if (primitive.isNumber())
            {
                return constant(primitive.getAsFloat());
            }
            if (primitive.isString())
            {
                String name = primitive.getAsString();
                Expression named = scope.resolve(name);
                if (named != null) return named;
                return ELAPSED_NAME.equals(name) ? ELAPSED : new Variable(scope.getVariables().read(name));
            }
            throw new MalformedKumoTemplateException("Not an expression: " + describe(json) + " (expected a number, a name or an operation).");
        }
        if (json.isJsonObject())
        {
            JsonObject object = json.getAsJsonObject();
            if (object.size() != 1)
            {
                throw new MalformedKumoTemplateException("An operation is an object with exactly one key, the operation's name: " + describe(json));
            }
            Map.Entry<String, JsonElement> entry = object.entrySet().iterator().next();
            ExpressionOperations.Operation operation = ExpressionOperations.get(entry.getKey());
            if (operation == null)
            {
                throw new MalformedKumoTemplateException("Unknown operation '" + entry.getKey() + "' in " + describe(json));
            }
            if (!entry.getValue().isJsonArray())
            {
                throw new MalformedKumoTemplateException("The arguments of '" + entry.getKey() + "' have to be a list: " + describe(json));
            }
            JsonArray argumentsJson = entry.getValue().getAsJsonArray();
            if (!operation.accepts(argumentsJson.size()))
            {
                throw new MalformedKumoTemplateException("'" + entry.getKey() + "' takes " + operation.describeArity() + ", not " + argumentsJson.size() + ": " + describe(json));
            }
            Expression[] arguments = new Expression[argumentsJson.size()];
            for (int i = 0; i < arguments.length; i++)
            {
                arguments[i] = compile(argumentsJson.get(i), scope);
            }
            return operation.create(arguments);
        }
        throw new MalformedKumoTemplateException("Not an expression: " + describe(json) + " (expected a number, a name or an operation).");
    }

    private static String describe(JsonElement json)
    {
        String text = json.toString();
        return text.length() > 120 ? text.substring(0, 117) + "..." : text;
    }

    static final class Constant extends Expression
    {
        private final float value;

        Constant(float value)
        {
            this.value = value;
        }

        @Override
        public float get(ITriggerConditionContext context)
        {
            return value;
        }
    }

    static final class Variable extends Expression
    {
        private final VariableTable.Read read;

        Variable(VariableTable.Read read)
        {
            this.read = read;
        }

        @Override
        public float get(ITriggerConditionContext context)
        {
            return (float) context.resolveVariable(read);
        }
    }

    static final class Elapsed extends Expression
    {
        @Override
        public float get(ITriggerConditionContext context)
        {
            INodeState node = context.getCurrentNode();
            return node == null ? 0F : node.getElapsedTicks();
        }
    }

}
