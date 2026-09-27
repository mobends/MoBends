package goblinbob.mobends.core.kumo.expr;

import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.util.Tween;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * The operations an expression can use, by name: {@code {"<name>": [arguments...]}}. Angles for
 * {@code sin}, {@code cos} and {@code atan2} are in radians.
 */
public final class ExpressionOperations
{

    /** Any number of arguments (from the minimum up). */
    public static final int VARIADIC = Integer.MAX_VALUE;

    @FunctionalInterface
    public interface Factory
    {
        Expression create(Expression[] arguments);
    }

    public static final class Operation
    {
        private final int minArguments;
        private final int maxArguments;
        private final Factory factory;

        Operation(int minArguments, int maxArguments, Factory factory)
        {
            this.minArguments = minArguments;
            this.maxArguments = maxArguments;
            this.factory = factory;
        }

        boolean accepts(int count)
        {
            return count >= minArguments && count <= maxArguments;
        }

        String describeArity()
        {
            if (maxArguments == VARIADIC) return minArguments + " or more arguments";
            if (minArguments == maxArguments) return minArguments == 1 ? "1 argument" : minArguments + " arguments";
            return minArguments + " to " + maxArguments + " arguments";
        }

        Expression create(Expression[] arguments)
        {
            return factory.create(arguments);
        }
    }

    private static final Map<String, Operation> OPERATIONS = new HashMap<>();

    static
    {
        // Folded left to right: {"sub": [a, b, c]} is (a - b) - c.
        fold("add", (a, b) -> a + b);
        fold("sub", (a, b) -> a - b);
        fold("mul", (a, b) -> a * b);
        fold("div", (a, b) -> a / b);
        fold("min", (a, b) -> b < a ? b : a);
        fold("max", (a, b) -> b > a ? b : a);

        // Floored: the result takes the divisor's sign, so {"mod": [-1, 20]} is 19.
        binary("mod", (a, b) -> a - (float) Math.floor(a / b) * b);
        binary("pow", (a, b) -> (float) Math.pow(a, b));
        binary("atan2", (y, x) -> (float) Math.atan2(y, x));

        unary("neg", a -> -a);
        unary("abs", Math::abs);
        unary("sqrt", a -> (float) Math.sqrt(a));
        unary("floor", a -> (float) Math.floor(a));
        unary("ceil", a -> (float) Math.ceil(a));
        unary("sin", a -> (float) Math.sin(a));
        unary("cos", a -> (float) Math.cos(a));
        // "mcsin" and "mccos" (Minecraft's table-based sine and cosine) are added by the game side.

        // {"clamp": [value, min, max]}
        register("clamp", 3, 3, args -> new Ternary(args, (value, min, max) -> {
            if (value < min) value = min;
            if (value > max) value = max;
            return value;
        }));
        // {"lerp": [from, to, t]}
        register("lerp", 3, 3, args -> new Ternary(args, (from, to, t) -> from + (to - from) * t));

        // {"easeIn": [t, power]}: shapes a 0..1 value.
        binary("easeIn", (t, power) -> (float) Tween.easeIn(t, power));
        binary("easeOut", (t, power) -> (float) Tween.easeOut(t, power));
        binary("easeInOut", (t, power) -> (float) Tween.easeInOut(t, power));
    }

    private ExpressionOperations()
    {
    }

    /** Adds an operation (addons may add their own, prefixed with their mod id). */
    public static void register(String name, int minArguments, int maxArguments, Factory factory)
    {
        OPERATIONS.put(name, new Operation(minArguments, maxArguments, factory));
    }

    @Nullable
    static Operation get(String name)
    {
        return OPERATIONS.get(name);
    }

    /** Adds a one-argument operation. */
    public static void unary(String name, UnaryFunction function)
    {
        register(name, 1, 1, args -> new Unary(args[0], function));
    }

    private static void binary(String name, BinaryFunction function)
    {
        register(name, 2, 2, args -> new Fold(args, function));
    }

    private static void fold(String name, BinaryFunction function)
    {
        register(name, 2, VARIADIC, args -> new Fold(args, function));
    }

    @FunctionalInterface
    public interface UnaryFunction
    {
        float apply(float a);
    }

    @FunctionalInterface
    private interface BinaryFunction
    {
        float apply(float a, float b);
    }

    @FunctionalInterface
    private interface TernaryFunction
    {
        float apply(float a, float b, float c);
    }

    private static final class Unary extends Expression
    {
        private final Expression argument;
        private final UnaryFunction function;

        Unary(Expression argument, UnaryFunction function)
        {
            this.argument = argument;
            this.function = function;
        }

        @Override
        public float get(ITriggerConditionContext context)
        {
            return function.apply(argument.get(context));
        }
    }

    private static final class Fold extends Expression
    {
        private final Expression[] arguments;
        private final BinaryFunction function;

        Fold(Expression[] arguments, BinaryFunction function)
        {
            this.arguments = arguments;
            this.function = function;
        }

        @Override
        public float get(ITriggerConditionContext context)
        {
            float value = arguments[0].get(context);
            for (int i = 1; i < arguments.length; i++)
            {
                value = function.apply(value, arguments[i].get(context));
            }
            return value;
        }
    }

    private static final class Ternary extends Expression
    {
        private final Expression a, b, c;
        private final TernaryFunction function;

        Ternary(Expression[] arguments, TernaryFunction function)
        {
            this.a = arguments[0];
            this.b = arguments[1];
            this.c = arguments[2];
            this.function = function;
        }

        @Override
        public float get(ITriggerConditionContext context)
        {
            return function.apply(a.get(context), b.get(context), c.get(context));
        }
    }

}
