package goblinbob.mobends.core.kumo.expr;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.Tween;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The operations an expression can use, by name: {@code {"<name>": [arguments...]}}. Each declares
 * its parameters, checked when the expression is compiled: a number or boolean expression, or a
 * string written out (an item id, one of a fixed set of choices). Bare names are the language;
 * {@code namespace:id} names are registered ({@code core:} by the engine, others by the host and
 * addons). Angles for {@code sin}, {@code cos} and {@code atan2} are in radians, for
 * {@code wrapDegrees} and {@code lerpAngle} in degrees.
 * <p>
 * Nothing short-circuits: {@code and}, {@code or} and {@code if} evaluate every argument, so an
 * edge trigger ({@code decreased}, {@code rose}, {@code fell}) never misses a frame.
 */
public final class ExpressionOperations
{

    /** What an argument has to be. */
    public enum Kind
    {
        NUMBER,
        BOOLEAN,
        /** A number or a boolean expression; the operation checks how its arguments' types go together. */
        ANY,
        /** A number written out, such as a window size: known when the animator loads. */
        CONSTANT,
        /** A string written out, such as an item id. */
        STRING,
        /** One of a fixed set of strings. */
        CHOICE
    }

    /** One parameter of an operation. */
    public static final class Param
    {
        final String name;
        final Kind kind;
        final String[] choices;

        private Param(String name, Kind kind, String... choices)
        {
            this.name = name;
            this.kind = kind;
            this.choices = choices;
        }
    }

    public static Param number(String name)
    {
        return new Param(name, Kind.NUMBER);
    }

    public static Param bool(String name)
    {
        return new Param(name, Kind.BOOLEAN);
    }

    public static Param any(String name)
    {
        return new Param(name, Kind.ANY);
    }

    public static Param string(String name)
    {
        return new Param(name, Kind.STRING);
    }

    public static Param choice(String name, String... choices)
    {
        return new Param(name, Kind.CHOICE, choices);
    }

    public static Param constant(String name)
    {
        return new Param(name, Kind.CONSTANT);
    }

    /** A parameter of any kind ({@code choices} for a choice). */
    public static Param param(String name, Kind kind, String... choices)
    {
        return new Param(name, kind, choices);
    }

    /** The compiled arguments of one use of an operation, for its {@link Factory}. */
    public static final class Arguments
    {
        private final String operation;
        private final Param[] params;
        private final Expression[] expressions;
        private final String[] strings;
        private final float[] constants;
        @Nullable
        private final Expression fallback;
        @Nullable
        private final Class<?> entityClass;
        private final boolean readsFields;
        private final boolean inSelector;
        private final goblinbob.mobends.core.kumo.state.StateLayout layout;

        Arguments(String operation, Param[] params, Expression[] expressions, String[] strings, float[] constants, @Nullable Expression fallback,
                  @Nullable Class<?> entityClass, boolean readsFields, boolean inSelector, goblinbob.mobends.core.kumo.state.StateLayout layout)
        {
            this.layout = layout;
            this.inSelector = inSelector;
            this.operation = operation;
            this.params = params;
            this.expressions = expressions;
            this.strings = strings;
            this.constants = constants;
            this.fallback = fallback;
            this.entityClass = entityClass;
            this.readsFields = readsFields;
        }

        /** The constant argument {@code index} (a number written out). */
        public float constant(int index)
        {
            return constants[index];
        }

        /** The class of the entity animated, or null if unknown. */
        @Nullable
        public Class<?> entityClass()
        {
            return entityClass;
        }

        /** Where the animator's per-entity state goes: a stateful operation takes its slots here. */
        public goblinbob.mobends.core.kumo.state.StateLayout layout()
        {
            return layout;
        }

        /** Whether the operation is written in a type file's selector. */
        public boolean inSelector()
        {
            return inSelector;
        }

        /** The operation's name. */
        public String operation()
        {
            return operation;
        }

        /** The {@code @fallback} written with the operation, or null (only for an operation that takes one). */
        @Nullable
        public Expression fallback()
        {
            return fallback;
        }

        /** The entity's class where the operation may read its fields (an entity definition), else null. */
        @Nullable
        public Class<?> fieldsOf()
        {
            return readsFields ? entityClass : null;
        }

        public int count()
        {
            return expressions.length;
        }

        /** The expression argument {@code index} (null for a string argument). */
        public Expression expression(int index)
        {
            return expressions[index];
        }

        public Expression[] expressions()
        {
            return expressions;
        }

        /** The string or choice argument {@code index}. */
        public String string(int index)
        {
            return strings[index];
        }

        /** An error about argument {@code index}, in the operation's own words. */
        public MalformedKumoTemplateException error(int index, String message)
        {
            return new MalformedKumoTemplateException("'" + operation + "' argument " + (index + 1) + " (" + params[Math.min(index, params.length - 1)].name + ") " + message);
        }
    }

    @FunctionalInterface
    public interface Factory
    {
        Expression create(Arguments arguments) throws MalformedKumoTemplateException;
    }

    public static final class Operation
    {
        private final Param[] params;
        /** Whether the last parameter repeats: the operation takes {@code params.length} or more arguments. */
        private final boolean repeatsLast;
        /** Whether it takes a {@code @fallback}: what it is when it can't be computed. */
        final boolean takesFallback;
        private final Factory factory;

        Operation(Param[] params, boolean repeatsLast, boolean takesFallback, Factory factory)
        {
            this.params = params;
            this.repeatsLast = repeatsLast;
            this.takesFallback = takesFallback;
            this.factory = factory;
        }

        private String describeArity()
        {
            int count = params.length;
            String arguments = count == 1 ? "1 argument" : count + " arguments";
            return repeatsLast ? count + " or more arguments" : arguments;
        }

        Expression compile(String name, JsonArray json, @Nullable JsonElement fallbackJson, ExpressionScope scope, JsonElement whole) throws MalformedKumoTemplateException
        {
            int count = json.size();
            if (repeatsLast ? count < params.length : count != params.length)
            {
                throw new MalformedKumoTemplateException("'" + name + "' takes " + describeArity() + ", not " + count + ": " + Expression.describe(whole));
            }
            Expression[] expressions = new Expression[count];
            String[] strings = new String[count];
            float[] constants = new float[count];
            for (int i = 0; i < count; i++)
            {
                Param param = params[Math.min(i, params.length - 1)];
                JsonElement argument = json.get(i);
                String where = "'" + name + "' argument " + (i + 1) + " (" + param.name + ")";
                switch (param.kind)
                {
                    case STRING:
                    case CHOICE:
                        if (argument == null || !argument.isJsonPrimitive() || !argument.getAsJsonPrimitive().isString())
                        {
                            throw new MalformedKumoTemplateException(where + " must be a string, got " + Expression.describe(argument) + ".");
                        }
                        strings[i] = argument.getAsString();
                        if (param.kind == Kind.CHOICE && !Arrays.asList(param.choices).contains(strings[i]))
                        {
                            throw new MalformedKumoTemplateException(where + " must be one of " + String.join(", ", param.choices) + ", got '" + strings[i] + "'.");
                        }
                        break;
                    case CONSTANT:
                        if (argument == null || !argument.isJsonPrimitive() || !argument.getAsJsonPrimitive().isNumber())
                        {
                            throw new MalformedKumoTemplateException(where + " must be a number written out, got " + Expression.describe(argument) + ".");
                        }
                        constants[i] = argument.getAsFloat();
                        break;
                    default:
                        expressions[i] = Expression.compileAny(argument, scope);
                        Expression.Type type = param.kind == Kind.NUMBER ? Expression.Type.NUMBER : param.kind == Kind.BOOLEAN ? Expression.Type.BOOLEAN : null;
                        if (type != null && expressions[i].getType() != type)
                        {
                            throw new MalformedKumoTemplateException(where + " must be " + type.description + ", got " + expressions[i].getType().description + ": " + Expression.describe(whole));
                        }
                }
            }
            Expression fallback = fallbackJson == null ? null : Expression.compileAny(fallbackJson, scope);
            try
            {
                Expression result = factory.create(new Arguments(name, params, expressions, strings, constants, fallback, scope.getEntityClass(), scope.getFieldsOf() != null, scope.isSelector(), scope.getLayout()));
                return foldable(name, result, expressions) ? fold(result) : result;
            }
            catch (MalformedKumoTemplateException e)
            {
                throw new MalformedKumoTemplateException(e.getMessage() + " In " + Expression.describe(whole));
            }
        }
    }

    /** The language operations that read the entity or remember something: never computed at load. */
    private static final java.util.Set<String> IMPURE = new java.util.HashSet<>(Arrays.asList("field", "exists", "decreased", "rose", "fell"));

    /**
     * Whether {@code result}, a language operation of constant arguments, is the same for every
     * entity and every frame: it is computed once, when the animator loads. (A registered pure
     * operation folds itself, see {@code BoundOperation}.)
     */
    private static boolean foldable(String name, Expression result, Expression[] arguments)
    {
        if (name.indexOf(':') >= 0 || IMPURE.contains(name) || result.isStateful() || result.isConstant())
        {
            return false;
        }
        for (Expression argument : arguments)
        {
            if (argument != null && !argument.isConstant()) return false;
        }
        return true;
    }

    private static Expression fold(Expression constant)
    {
        return constant.getType() == Expression.Type.BOOLEAN ? (constant.test(null) ? Expression.TRUE : Expression.FALSE) : Expression.constant(constant.get(null));
    }

    private static final Map<String, Operation> OPERATIONS = new HashMap<>();
    private static final Map<String, goblinbob.mobends.core.kumo.api.KumoOperation> REGISTERED = new HashMap<>();

    /** Minecraft's sine table ({@code MathHelper.SIN_TABLE}): 65536 steps of a full turn. */
    private static final float[] SIN_TABLE = new float[65536];

    static
    {
        for (int i = 0; i < SIN_TABLE.length; i++)
        {
            SIN_TABLE[i] = (float) Math.sin((double) i * Math.PI * 2.0D / 65536.0D);
        }

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
        // Minecraft's table-based sine and cosine, which vanilla models use: values match them exactly.
        unary("mcsin", a -> SIN_TABLE[(int) (a * 10430.378F) & 65535]);
        unary("mccos", a -> SIN_TABLE[(int) (a * 10430.378F + 16384.0F) & 65535]);
        unary("wrapDegrees", ExpressionOperations::wrapDegrees);

        ternary("clamp", "value", "min", "max", (value, min, max) -> {
            if (value < min) value = min;
            if (value > max) value = max;
            return value;
        });
        ternary("lerp", "from", "to", "t", (from, to, t) -> from + (to - from) * t);
        // The short way round: from 350 to 10 goes through 0.
        ternary("lerpAngle", "from", "to", "t", (from, to, t) -> from + wrapDegrees(to - from) * t);
        // 0 below edge0, 1 above edge1, linear (smooth) between.
        ternary("linstep", "x", "edge0", "edge1", ExpressionOperations::linstep);
        ternary("smoothstep", "x", "edge0", "edge1", (x, edge0, edge1) -> {
            float t = linstep(x, edge0, edge1);
            return t * t * (3 - 2 * t);
        });

        // {"easeIn": [t, power]}: shapes a 0..1 value.
        binary("easeIn", (t, power) -> (float) Tween.easeIn(t, power));
        binary("easeOut", (t, power) -> (float) Tween.easeOut(t, power));
        binary("easeInOut", (t, power) -> (float) Tween.easeInOut(t, power));

        compare("lt", (a, b) -> a < b);
        compare("le", (a, b) -> a <= b);
        compare("gt", (a, b) -> a > b);
        compare("ge", (a, b) -> a >= b);
        equality("eq", true);
        equality("ne", false);

        register("and", params(bool("condition")), true, args -> new Logic(args.expressions(), true));
        register("or", params(bool("condition")), true, args -> new Logic(args.expressions(), false));
        register("not", params(bool("condition")), false, args -> new Not(args.expression(0)));
        register("if", params(bool("condition"), any("then"), any("else")), false, args -> {
            if (args.expression(1).getType() != args.expression(2).getType())
            {
                throw new MalformedKumoTemplateException("'if' needs two branches of the same type, got " + args.expression(1).getType().description
                        + " and " + args.expression(2).getType().description + ".");
            }
            return args.expression(1).getType() == Expression.Type.NUMBER ? new IfNumber(args.expressions()) : new IfBoolean(args.expressions());
        });

        register("decreased", params(number("value")), false, args -> new Decreased(args.expression(0), args.layout().floats(2, 0)));
        register("rose", params(bool("condition")), false, args -> new Edge(args.expression(0), true, args.layout().floats(2, 0)));
        register("fell", params(bool("condition")), false, args -> new Edge(args.expression(0), false, args.layout().floats(2, 0)));

        // The entity's own fields, read only by its model definition's definitions.
        registerWithFallback("field", params(string("field")), true, ExpressionOperations::field);
        register("exists", params(string("field")), true, args -> {
            EntityFields.Path path = resolveField(args);
            return path == null ? Expression.FALSE : new FieldExists(path);
        });
    }

    private static Expression field(Arguments args) throws MalformedKumoTemplateException
    {
        EntityFields.Path path = resolveField(args);
        Expression fallback = args.fallback();
        if (path == null)
        {
            if (fallback == null)
            {
                throw new MalformedKumoTemplateException(String.format("The entity has no field '%s' (and the 'field' has no \"@fallback\").", fieldPath(args)));
            }
            return fallback;
        }
        if (fallback != null && fallback.getType() != path.type())
        {
            throw new MalformedKumoTemplateException(String.format("The field '%s' is %s, but its \"@fallback\" is %s.", fieldPath(args),
                    path.type().description, fallback.getType().description));
        }
        return path.type() == Expression.Type.BOOLEAN ? new FieldBoolean(path, fallback) : new FieldNumber(path, fallback);
    }

    /** The path a {@code field} or an {@code exists} names, or null if the entity has none such. */
    @Nullable
    private static EntityFields.Path resolveField(Arguments args) throws MalformedKumoTemplateException
    {
        if (args.fieldsOf() == null)
        {
            throw new MalformedKumoTemplateException("The entity's fields are read only in a model definition's \"@define\" (as entity.* definitions).");
        }
        String[] steps = new String[args.count()];
        for (int i = 0; i < steps.length; i++)
        {
            steps[i] = args.string(i);
        }
        return EntityFields.Holder.fields.resolve(args.fieldsOf(), Arrays.asList(steps));
    }

    private static String fieldPath(Arguments args)
    {
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < args.count(); i++)
        {
            if (i > 0) path.append('.');
            path.append(args.string(i));
        }
        return path.toString();
    }

    private ExpressionOperations()
    {
    }

    public static Param[] params(Param... params)
    {
        return params;
    }

    /**
     * Adds an operation (addons may add their own, prefixed with their mod id).
     *
     * @param repeatsLast whether the last parameter repeats, so the operation takes as many
     *                    arguments as it has parameters, or more
     */
    public static void register(String name, Param[] params, boolean repeatsLast, Factory factory)
    {
        OPERATIONS.put(name, new Operation(params, repeatsLast, false, factory));
    }

    /**
     * Adds a registered operation (see {@link goblinbob.mobends.core.kumo.api.KumoRegistry}): its
     * name must be namespaced, and not taken by an operation or a driver.
     */
    public static void register(goblinbob.mobends.core.kumo.api.KumoOperation operation)
    {
        if (operation.name.indexOf(':') <= 0)
        {
            throw new IllegalArgumentException("A registered operation's name is namespaced ('mymod:" + operation.name + "'): bare names are the language's.");
        }
        if (OPERATIONS.containsKey(operation.name))
        {
            throw new IllegalArgumentException("The operation '" + operation.name + "' is already registered.");
        }
        if (goblinbob.mobends.core.kumo.driver.DriverRegistry.INSTANCE.getTemplateClass(operation.name) != null)
        {
            // Operations and drivers share one namespace: core:spring names one thing.
            throw new IllegalArgumentException("'" + operation.name + "' is already registered as a driver.");
        }
        OPERATIONS.put(operation.name, new Operation(operation.compilerParams(), operation.repeatsLast, operation.takesFallback,
                args -> BoundOperation.compile(operation, args)));
        REGISTERED.put(operation.name, operation);
    }

    /** The registered operation {@code name} (see {@link #register(goblinbob.mobends.core.kumo.api.KumoOperation)}), or null. */
    @Nullable
    public static goblinbob.mobends.core.kumo.api.KumoOperation registered(String name)
    {
        return REGISTERED.get(name);
    }

    /**
     * Adds an operation that may be written with a {@code @fallback}: what it is when it can't be
     * computed (see {@link Arguments#fallback}).
     */
    public static void registerWithFallback(String name, Param[] params, boolean repeatsLast, Factory factory)
    {
        OPERATIONS.put(name, new Operation(params, repeatsLast, true, factory));
    }

    @Nullable
    static Operation get(String name)
    {
        return OPERATIONS.get(name);
    }

    /** Adds a one-argument number operation. */
    public static void unary(String name, UnaryFunction function)
    {
        register(name, params(number("value")), false, args -> new Unary(args.expression(0), function));
    }

    private static void binary(String name, BinaryFunction function)
    {
        register(name, params(number("a"), number("b")), false, args -> new Fold(args.expressions(), function));
    }

    private static void fold(String name, BinaryFunction function)
    {
        register(name, params(number("a"), number("b")), true, args -> new Fold(args.expressions(), function));
    }

    private static void ternary(String name, String a, String b, String c, TernaryFunction function)
    {
        register(name, params(number(a), number(b), number(c)), false, args -> new Ternary(args.expressions(), function));
    }

    private static void compare(String name, Comparison comparison)
    {
        register(name, params(number("a"), number("b")), false, args -> new Compare(args.expression(0), args.expression(1), comparison));
    }

    private static void equality(String name, boolean equal)
    {
        register(name, params(any("a"), any("b")), false, args -> {
            if (args.expression(0).getType() != args.expression(1).getType())
            {
                throw new MalformedKumoTemplateException("'" + name + "' compares two numbers or two booleans, got " + args.expression(0).getType().description
                        + " and " + args.expression(1).getType().description + ".");
            }
            return new Equality(args.expression(0), args.expression(1), equal);
        });
    }

    public static float wrapDegrees(float degrees)
    {
        degrees %= 360;
        if (degrees >= 180) degrees -= 360;
        if (degrees < -180) degrees += 360;
        return degrees;
    }

    private static float linstep(float x, float edge0, float edge1)
    {
        if (edge1 == edge0)
        {
            return x < edge0 ? 0 : 1;
        }
        float t = (x - edge0) / (edge1 - edge0);
        return t < 0 ? 0 : t > 1 ? 1 : t;
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

    @FunctionalInterface
    private interface Comparison
    {
        boolean test(float a, float b);
    }

    static boolean anyStateful(Expression... expressions)
    {
        for (Expression expression : expressions)
        {
            if (expression.isStateful()) return true;
        }
        return false;
    }

    static void restartAll(ITriggerConditionContext context, Expression... expressions)
    {
        for (Expression expression : expressions)
        {
            if (expression.isStateful()) expression.restart(context);
        }
    }

    /** A number operation over number arguments. */
    private abstract static class NumberOp extends Expression.NumberExpression
    {
        final Expression[] arguments;
        private final boolean stateful;

        NumberOp(Expression... arguments)
        {
            this.arguments = arguments;
            this.stateful = anyStateful(arguments);
        }

        @Override
        public boolean isStateful()
        {
            return stateful;
        }

        @Override
        public void restart(ITriggerConditionContext context)
        {
            restartAll(context, arguments);
        }
    }

    /** A boolean operation over its arguments. */
    private abstract static class BooleanOp extends Expression.BooleanExpression
    {
        final Expression[] arguments;
        private final boolean stateful;

        BooleanOp(Expression... arguments)
        {
            this.arguments = arguments;
            this.stateful = anyStateful(arguments);
        }

        @Override
        public boolean isStateful()
        {
            return stateful;
        }

        @Override
        public void restart(ITriggerConditionContext context)
        {
            restartAll(context, arguments);
        }
    }

    private static final class Unary extends NumberOp
    {
        private final UnaryFunction function;

        Unary(Expression argument, UnaryFunction function)
        {
            super(argument);
            this.function = function;
        }

        @Override
        public float get(ITriggerConditionContext context)
        {
            return function.apply(arguments[0].get(context));
        }
    }

    private static final class Fold extends NumberOp
    {
        private final BinaryFunction function;

        Fold(Expression[] arguments, BinaryFunction function)
        {
            super(arguments);
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

    private static final class Ternary extends NumberOp
    {
        private final TernaryFunction function;

        Ternary(Expression[] arguments, TernaryFunction function)
        {
            super(arguments);
            this.function = function;
        }

        @Override
        public float get(ITriggerConditionContext context)
        {
            return function.apply(arguments[0].get(context), arguments[1].get(context), arguments[2].get(context));
        }
    }

    private static final class Compare extends BooleanOp
    {
        private final Comparison comparison;

        Compare(Expression a, Expression b, Comparison comparison)
        {
            super(a, b);
            this.comparison = comparison;
        }

        @Override
        public boolean test(ITriggerConditionContext context)
        {
            return comparison.test(arguments[0].get(context), arguments[1].get(context));
        }
    }

    private static final class Equality extends BooleanOp
    {
        private final boolean equal;

        Equality(Expression a, Expression b, boolean equal)
        {
            super(a, b);
            this.equal = equal;
        }

        @Override
        public boolean test(ITriggerConditionContext context)
        {
            boolean same = arguments[0].getType() == Expression.Type.BOOLEAN
                    ? arguments[0].test(context) == arguments[1].test(context)
                    : arguments[0].get(context) == arguments[1].get(context);
            return same == equal;
        }
    }

    /** {@code and} / {@code or}: every argument is evaluated, whatever the first ones gave. */
    private static final class Logic extends BooleanOp
    {
        private final boolean all;

        Logic(Expression[] arguments, boolean all)
        {
            super(arguments);
            this.all = all;
        }

        @Override
        public boolean test(ITriggerConditionContext context)
        {
            boolean result = all;
            for (Expression argument : arguments)
            {
                boolean value = argument.test(context);
                result = all ? result && value : result || value;
            }
            return result;
        }
    }

    /** The object a field path reads from on this frame's entity, or null where a step is null. */
    @Nullable
    private static Object fieldOwner(EntityFields.Path path, ITriggerConditionContext context)
    {
        Object entity = context.getSubject().getEntity();
        return entity == null ? null : path.owner(entity);
    }

    /** A number field; its fallback (or 0) where a step of its path is null. */
    private static final class FieldNumber extends NumberOp
    {
        private final EntityFields.Path path;

        FieldNumber(EntityFields.Path path, @Nullable Expression fallback)
        {
            super(fallback == null ? new Expression[0] : new Expression[] { fallback });
            this.path = path;
        }

        @Override
        public float get(ITriggerConditionContext context)
        {
            Object owner = fieldOwner(path, context);
            if (owner == null)
            {
                return arguments.length == 0 ? 0 : arguments[0].get(context);
            }
            return (float) path.number(owner);
        }
    }

    /** A boolean field; its fallback (or false) where a step of its path is null. */
    private static final class FieldBoolean extends BooleanOp
    {
        private final EntityFields.Path path;

        FieldBoolean(EntityFields.Path path, @Nullable Expression fallback)
        {
            super(fallback == null ? new Expression[0] : new Expression[] { fallback });
            this.path = path;
        }

        @Override
        public boolean test(ITriggerConditionContext context)
        {
            Object owner = fieldOwner(path, context);
            if (owner == null)
            {
                return arguments.length != 0 && arguments[0].test(context);
            }
            return path.bool(owner);
        }
    }

    /** Whether every step of a field path is there (none null) on this frame. */
    private static final class FieldExists extends BooleanOp
    {
        private final EntityFields.Path path;

        FieldExists(EntityFields.Path path)
        {
            this.path = path;
        }

        @Override
        public boolean test(ITriggerConditionContext context)
        {
            return fieldOwner(path, context) != null;
        }
    }

    private static final class Not extends BooleanOp
    {
        Not(Expression argument)
        {
            super(argument);
        }

        @Override
        public boolean test(ITriggerConditionContext context)
        {
            return !arguments[0].test(context);
        }
    }

    /** {@code if} between numbers: both branches are evaluated. */
    private static final class IfNumber extends NumberOp
    {
        IfNumber(Expression[] arguments)
        {
            super(arguments);
        }

        @Override
        public float get(ITriggerConditionContext context)
        {
            boolean condition = arguments[0].test(context);
            float then = arguments[1].get(context);
            float otherwise = arguments[2].get(context);
            return condition ? then : otherwise;
        }
    }

    /** {@code if} between booleans: both branches are evaluated. */
    private static final class IfBoolean extends BooleanOp
    {
        IfBoolean(Expression[] arguments)
        {
            super(arguments);
        }

        @Override
        public boolean test(ITriggerConditionContext context)
        {
            boolean condition = arguments[0].test(context);
            boolean then = arguments[1].test(context);
            boolean otherwise = arguments[2].test(context);
            return condition ? then : otherwise;
        }
    }

    /**
     * Holds on the frame its value is lower than on the previous evaluation (e.g.
     * {@code ticksAfterAttack} going back to 0 on a new attack). On a restart it notes the value
     * as it is then.
     */
    private static final class Decreased extends Expression.BooleanExpression
    {
        private final Expression value;
        /** Its state: the last value, then whether there is one (1). */
        private final int slot;

        Decreased(Expression value, int slot)
        {
            this.value = value;
            this.slot = slot;
        }

        @Override
        public boolean isStateful()
        {
            return true;
        }

        @Override
        public void restart(ITriggerConditionContext context)
        {
            value.restart(context);
            float[] state = context.getState().floats;
            state[slot] = value.get(context);
            state[slot + 1] = 1;
        }

        @Override
        public boolean test(ITriggerConditionContext context)
        {
            float current = value.get(context);
            float[] state = context.getState().floats;
            boolean decreased = state[slot + 1] != 0 && current < state[slot];
            state[slot] = current;
            state[slot + 1] = 1;
            return decreased;
        }
    }

    /** {@code rose} / {@code fell}: holds on the frame its condition turns true / false. */
    private static final class Edge extends Expression.BooleanExpression
    {
        private final Expression condition;
        private final boolean rising;
        /** Its state: the last value (0 or 1), then whether there is one (1). */
        private final int slot;

        Edge(Expression condition, boolean rising, int slot)
        {
            this.condition = condition;
            this.rising = rising;
            this.slot = slot;
        }

        @Override
        public boolean isStateful()
        {
            return true;
        }

        @Override
        public void restart(ITriggerConditionContext context)
        {
            condition.restart(context);
            float[] state = context.getState().floats;
            state[slot] = condition.test(context) ? 1 : 0;
            state[slot + 1] = 1;
        }

        @Override
        public boolean test(ITriggerConditionContext context)
        {
            boolean current = condition.test(context);
            float[] state = context.getState().floats;
            boolean edge = state[slot + 1] != 0 && current != (state[slot] != 0) && current == rising;
            state[slot] = current ? 1 : 0;
            state[slot + 1] = 1;
            return edge;
        }
    }

}
