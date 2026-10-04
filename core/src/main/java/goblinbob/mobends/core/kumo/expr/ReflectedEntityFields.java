package goblinbob.mobends.core.kumo.expr;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * Resolves {@code field} paths by reflection: each step is a field declared by the step before's
 * declared type or one of its superclasses. A host whose fields are renamed at runtime (Minecraft,
 * obfuscated) overrides {@link #step} to find those its own way first.
 */
public class ReflectedEntityFields implements EntityFields
{

    /** One step of a path: a field, read from the object the step before it gave. */
    public static final class Step
    {
        /** The field's declared type: what the next step is looked up on. */
        final Class<?> type;
        final Reader reader;
        /** Reads a number field without boxing it; null to go through {@link #reader}. */
        @Nullable
        final ToDoubleFunction<Object> number;

        public Step(Class<?> type, Reader reader, @Nullable ToDoubleFunction<Object> number)
        {
            this.type = type;
            this.reader = reader;
            this.number = number;
        }
    }

    @FunctionalInterface
    public interface Reader
    {
        /** The field's value (boxed), or null if it is null or can't be read. */
        @Nullable
        Object read(Object owner);
    }

    @Nullable
    @Override
    public Path resolve(Class<?> type, List<String> path)
    {
        if (path.isEmpty())
        {
            return null;
        }
        Step[] steps = new Step[path.size()];
        Class<?> at = type;
        for (int i = 0; i < steps.length; i++)
        {
            steps[i] = step(at, path.get(i));
            if (steps[i] == null)
            {
                return null;
            }
            at = steps[i].type;
        }
        Expression.Type result = typeOf(at);
        return result == null ? null : new StepPath(steps, result);
    }

    /** The field {@code name} that {@code type} or a superclass declares, or null. */
    @Nullable
    protected Step step(Class<?> type, String name)
    {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass())
        {
            Field field;
            try
            {
                field = c.getDeclaredField(name);
                field.setAccessible(true);
            }
            catch (NoSuchFieldException | SecurityException e)
            {
                continue;
            }
            ToDoubleFunction<Object> number = !field.getType().isPrimitive() || field.getType() == boolean.class ? null : owner -> {
                try
                {
                    return field.getDouble(owner);
                }
                catch (IllegalAccessException | IllegalArgumentException e)
                {
                    return 0;
                }
            };
            return new Step(field.getType(), owner -> {
                try
                {
                    return field.get(owner);
                }
                catch (IllegalAccessException | IllegalArgumentException e)
                {
                    return null;
                }
            }, number);
        }
        return null;
    }

    /** What a field of {@code type} is read as, or null if it is neither a number nor a boolean. */
    @Nullable
    private static Expression.Type typeOf(Class<?> type)
    {
        if (type == boolean.class || type == Boolean.class)
        {
            return Expression.Type.BOOLEAN;
        }
        if (type.isPrimitive() && type != char.class && type != void.class || Number.class.isAssignableFrom(type))
        {
            return Expression.Type.NUMBER;
        }
        return null;
    }

    private static final class StepPath implements Path
    {
        private final Step[] steps;
        private final Expression.Type type;

        StepPath(Step[] steps, Expression.Type type)
        {
            this.steps = steps;
            this.type = type;
        }

        @Override
        public Expression.Type type()
        {
            return type;
        }

        @Nullable
        @Override
        public Object owner(Object entity)
        {
            Object owner = entity;
            for (int i = 0; i < steps.length - 1 && owner != null; i++)
            {
                owner = steps[i].reader.read(owner);
            }
            return owner;
        }

        @Override
        public double number(Object owner)
        {
            Step last = steps[steps.length - 1];
            if (last.number != null)
            {
                return last.number.applyAsDouble(owner);
            }
            Object value = last.reader.read(owner);
            return value instanceof Number ? ((Number) value).doubleValue() : 0;
        }

        @Override
        public boolean bool(Object owner)
        {
            return Boolean.TRUE.equals(steps[steps.length - 1].reader.read(owner));
        }
    }

}
