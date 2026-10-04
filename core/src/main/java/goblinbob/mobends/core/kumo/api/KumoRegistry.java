package goblinbob.mobends.core.kumo.api;

import goblinbob.mobends.core.kumo.driver.DriverRegistry;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations.Kind;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * Where operations are registered, at three levels: a pure function of numbers, a reader of the
 * entity, and the full {@link KumoOperation}. Names are namespaced ({@code mymod:smoothstep});
 * bare names are the language's. An addon registers through its {@code AddonAnimationRegistry},
 * which adds its mod id.
 */
public final class KumoRegistry
{

    /** Set once the first animator has loaded: what it compiled against can't change any more. */
    private static boolean closed;

    private KumoRegistry()
    {
    }

    /** Refuses registrations from now on; the host calls it when its first animator loads. */
    public static void close()
    {
        closed = true;
    }

    /** Throws if registrations are refused (see {@link #close}). */
    public static void checkOpen()
    {
        if (closed)
        {
            throw new IllegalStateException("Animation content has to be registered before the first animator loads (an addon registers it from IAddon.registerContent).");
        }
    }

    public static void registerOperation(KumoOperation operation)
    {
        checkOpen();
        ExpressionOperations.register(operation);
    }

    /** Registers a driver, {@code {"mymod:wag": {...}}}; see {@link KumoDriver}. */
    public static void registerDriver(KumoDriver<?> driver)
    {
        checkOpen();
        DriverRegistry.INSTANCE.register(driver);
    }

    /** {@code {"mymod:smoothstep": [t]}}: computed once, when the animator loads, if its argument is written out. */
    public static void registerFunction(String name, NumberFunctions.Unary function)
    {
        registerOperation(KumoOperation.named(name).param("a", Kind.NUMBER).returns(Expression.Type.NUMBER).pure()
                .bind(args -> (NumberEvaluator) (context, values) -> function.apply(values.number(0))));
    }

    public static void registerFunction(String name, NumberFunctions.Binary function)
    {
        registerOperation(KumoOperation.named(name).param("a", Kind.NUMBER).param("b", Kind.NUMBER).returns(Expression.Type.NUMBER).pure()
                .bind(args -> (NumberEvaluator) (context, values) -> function.apply(values.number(0), values.number(1))));
    }

    public static void registerFunction(String name, NumberFunctions.Ternary function)
    {
        registerOperation(KumoOperation.named(name).param("a", Kind.NUMBER).param("b", Kind.NUMBER).param("c", Kind.NUMBER)
                .returns(Expression.Type.NUMBER).pure()
                .bind(args -> (NumberEvaluator) (context, values) -> function.apply(values.number(0), values.number(1), values.number(2))));
    }

    /**
     * {@code {"mymod:wetness": []}}: a number read from the entity. {@code type} is where it
     * applies: an entity of another class takes the {@code @fallback}, or the animator fails to
     * load. The reader gets the entity as a {@code type}; it never casts.
     */
    public static <E> void registerEntityNumber(String name, Class<E> type, ToDoubleFunction<? super E> reader)
    {
        registerOperation(KumoOperation.named(name).returns(Expression.Type.NUMBER).withFallback()
                .bind(args -> applies(type, args) ? (NumberEvaluator) (context, values) -> {
                    Object entity = context.entity();
                    return entity == null ? 0 : (float) reader.applyAsDouble(type.cast(entity));
                } : null));
    }

    /** {@code {"mymod:is_wet": []}}: whether the entity is something; see {@link #registerEntityNumber}. */
    public static <E> void registerEntityCondition(String name, Class<E> type, Predicate<? super E> reader)
    {
        registerOperation(KumoOperation.named(name).returns(Expression.Type.BOOLEAN).withFallback()
                .bind(args -> applies(type, args) ? (BooleanEvaluator) (context, values) -> {
                    Object entity = context.entity();
                    return entity != null && reader.test(type.cast(entity));
                } : null));
    }

    private static boolean applies(Class<?> type, BindArgs args)
    {
        return args.entityClass() != null && type.isAssignableFrom(args.entityClass());
    }

}
