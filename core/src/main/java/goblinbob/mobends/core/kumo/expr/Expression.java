package goblinbob.mobends.core.kumo.expr;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import goblinbob.mobends.core.kumo.state.VariableTable;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A value computed every frame: a tree of operations over constants and names, compiled once from
 * its JSON form ({@link ExpressionTemplate}). Every expression is a number or a boolean, checked
 * when it is compiled; arithmetic is in {@code float}.
 * <p>
 * A few operations remember something between frames ({@code decreased}, {@code rose},
 * {@code fell}): each place they are written keeps its own memory, which {@link #restart} starts
 * over when the scope holding that place starts (a node, a machine, a layer).
 */
public abstract class Expression
{

    public enum Type
    {
        NUMBER("a number"),
        BOOLEAN("a boolean");

        final String description;

        Type(String description)
        {
            this.description = description;
        }
    }

    public static final Expression ZERO = constant(0F);
    public static final Expression ONE = constant(1F);
    public static final Expression TRUE = new BooleanConstant(true);
    public static final Expression FALSE = new BooleanConstant(false);

    /**
     * The built-in values of the node and the layer being evaluated, by name (see
     * misc/kumo-format.md, *Nodes, transitions and time*).
     */
    private static final Map<String, Expression> BUILT_INS = new HashMap<>();
    /** A name in capitals is one of the subject's states, a boolean (e.g. {@code ON_GROUND}). */
    private static final Pattern STATE_NAME = Pattern.compile("[A-Z][A-Z0-9_]*");

    public abstract Type getType();

    /** The value of a number expression (a boolean reads 1 or 0). */
    public abstract float get(ITriggerConditionContext context);

    /** The value of a boolean expression. */
    public abstract boolean test(ITriggerConditionContext context);

    /** Whether any part of the expression remembers something between frames. */
    public boolean isStateful()
    {
        return false;
    }

    /** Starts the memory of the stateful operations in the expression over. */
    public void restart(ITriggerConditionContext context)
    {
    }

    public static Expression constant(float value)
    {
        return new Constant(value);
    }

    /**
     * Compiles the number expression {@code template} against {@code scope}, or returns
     * {@code fallback} if there is none.
     */
    public static Expression compile(@Nullable ExpressionTemplate template, ExpressionScope scope, Expression fallback) throws MalformedKumoTemplateException
    {
        return template == null ? fallback : compile(template.json, scope);
    }

    /** Compiles a number expression. */
    public static Expression compile(JsonElement json, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        return compile(json, scope, Type.NUMBER);
    }

    /** Compiles the condition {@code template} (a boolean expression), or returns null if there is none. */
    @Nullable
    public static Expression compileCondition(@Nullable ExpressionTemplate template, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        return template == null ? null : compile(template.json, scope, Type.BOOLEAN);
    }

    /** Compiles an expression that has to be of type {@code expected}. */
    public static Expression compile(JsonElement json, ExpressionScope scope, Type expected) throws MalformedKumoTemplateException
    {
        Expression expression = compileAny(json, scope);
        requireType(expression, expected, json);
        scope.held(expression);
        return expression;
    }

    static void requireType(Expression expression, Type expected, JsonElement json) throws MalformedKumoTemplateException
    {
        if (expression.getType() != expected)
        {
            throw new MalformedKumoTemplateException("Expected " + expected.description + ", got " + expression.getType().description + ": " + describe(json));
        }
    }

    /** Compiles an expression of either type. */
    static Expression compileAny(JsonElement json, ExpressionScope scope) throws MalformedKumoTemplateException
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
            if (primitive.isBoolean())
            {
                return primitive.getAsBoolean() ? TRUE : FALSE;
            }
            return compileName(primitive.getAsString(), scope);
        }
        if (json.isJsonObject())
        {
            // One key, the operation's name, and the modifiers: "@comment" anywhere, "@fallback"
            // where the operation takes one.
            Map.Entry<String, JsonElement> entry = null;
            JsonElement fallback = null;
            for (Map.Entry<String, JsonElement> key : json.getAsJsonObject().entrySet())
            {
                if (key.getKey().equals("@comment"))
                {
                    continue;
                }
                if (key.getKey().equals("@fallback"))
                {
                    fallback = key.getValue();
                    continue;
                }
                if (key.getKey().startsWith("@"))
                {
                    throw new MalformedKumoTemplateException("Unknown modifier '" + key.getKey() + "' on an operation (it takes \"@comment\", or \"@fallback\" where the operation does): " + describe(json));
                }
                if (entry != null)
                {
                    throw new MalformedKumoTemplateException("An operation is an object with exactly one key, the operation's name: " + describe(json));
                }
                entry = key;
            }
            if (entry == null)
            {
                throw new MalformedKumoTemplateException("An operation is an object with exactly one key, the operation's name: " + describe(json));
            }
            ExpressionOperations.Operation operation = ExpressionOperations.get(entry.getKey());
            if (operation == null)
            {
                throw new MalformedKumoTemplateException("Unknown operation '" + entry.getKey() + "' in " + describe(json));
            }
            if (!entry.getValue().isJsonArray())
            {
                throw new MalformedKumoTemplateException("The arguments of '" + entry.getKey() + "' have to be a list: " + describe(json));
            }
            if (fallback != null && !operation.takesFallback)
            {
                throw new MalformedKumoTemplateException("'" + entry.getKey() + "' takes no \"@fallback\": it can always be computed. In " + describe(json));
            }
            return operation.compile(entry.getKey(), entry.getValue().getAsJsonArray(), fallback, scope, json);
        }
        throw new MalformedKumoTemplateException("Not an expression: " + describe(json) + " (expected a number, a boolean, a name or an operation).");
    }

    private static Expression compileName(String name, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        if (name.indexOf('.') >= 0)
        {
            return scope.resolveScoped(name);
        }
        Expression value = scope.value(name);
        if (value != null)
        {
            return value;
        }
        Expression builtIn = BUILT_INS.get(name);
        if (builtIn != null)
        {
            return builtIn;
        }
        if (STATE_NAME.matcher(name).matches())
        {
            return new State(scope.getVariables().state(name));
        }
        return new Variable(scope.getVariables().read(name));
    }

    static String describe(JsonElement json)
    {
        String text = json.toString();
        return text.length() > 120 ? text.substring(0, 117) + "..." : text;
    }

    /** An expression whose value is a number. */
    public abstract static class NumberExpression extends Expression
    {
        @Override
        public final Type getType()
        {
            return Type.NUMBER;
        }

        @Override
        public boolean test(ITriggerConditionContext context)
        {
            return get(context) != 0;
        }
    }

    /** An expression whose value is a boolean. */
    public abstract static class BooleanExpression extends Expression
    {
        @Override
        public final Type getType()
        {
            return Type.BOOLEAN;
        }

        @Override
        public float get(ITriggerConditionContext context)
        {
            return test(context) ? 1F : 0F;
        }
    }

    static final class Constant extends NumberExpression
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

    static final class BooleanConstant extends BooleanExpression
    {
        private final boolean value;

        BooleanConstant(boolean value)
        {
            this.value = value;
        }

        @Override
        public boolean test(ITriggerConditionContext context)
        {
            return value;
        }
    }

    static final class Variable extends NumberExpression
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

    static final class State extends BooleanExpression
    {
        private final VariableTable.State state;

        State(VariableTable.State state)
        {
            this.state = state;
        }

        @Override
        public boolean test(ITriggerConditionContext context)
        {
            return state.get(context.getSubject());
        }
    }

    private static void number(String name, NumberValue value)
    {
        BUILT_INS.put(name, new NumberExpression()
        {
            @Override
            public float get(ITriggerConditionContext context)
            {
                return value.get(context);
            }
        });
    }

    private static void bool(String name, BooleanValue value)
    {
        BUILT_INS.put(name, new BooleanExpression()
        {
            @Override
            public boolean test(ITriggerConditionContext context)
            {
                return value.test(context);
            }
        });
    }

    @FunctionalInterface
    private interface NumberValue
    {
        float get(ITriggerConditionContext context);
    }

    @FunctionalInterface
    private interface BooleanValue
    {
        boolean test(ITriggerConditionContext context);
    }

    static
    {
        // Ticks since the node being evaluated was entered.
        number("nodeTicksElapsed", context -> context.getCurrentNode() == null ? 0F : context.getCurrentNode().getElapsedTicks());
        // Ticks since the layer started.
        number("layerTicksElapsed", context -> context.getLayerState() == null ? 0F : context.getLayerState().getElapsedTicks());
        // The linear progress of the crossfade the node is part of: 0 when it starts, 1 when it ends, 1 with none.
        number("nodeFadeProgress", context -> context.getLayerState() == null ? 1F : context.getLayerState().getFadeProgress(context.getCurrentNode()));
        bool("nodeIsFadingIn", context -> context.getLayerState() != null && context.getLayerState().isFadingIn(context.getCurrentNode()));
        bool("nodeIsActive", context -> context.getLayerState() != null && context.getLayerState().isActive(context.getCurrentNode()));
        bool("nodeIsFadingOut", context -> context.getLayerState() != null && context.getLayerState().isFadingOut(context.getCurrentNode()));
        // The node's timed clips have run (see INodeState#isAnimationFinished).
        bool("nodeIsFinished", context -> context.getCurrentNode() != null && context.getCurrentNode().isAnimationFinished());
    }

}
