package goblinbob.mobends.core.definition;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * Finds the fields a model definition names, by their development-time (MCP) names. For each class
 * of the hierarchy, the generated accessor table is tried first: vanilla fields are renamed in
 * production, so only accessors compiled against them (and reobfuscated with the mod) find them
 * there. Anything else, a mod's own entity or model in particular, is not obfuscated and is found
 * by reflection.
 */
public final class DefinedFields
{

    /** Generated accessors: the one for the field {@code name} declared by {@code owner} (a binary class name), or null. */
    public interface Table<A>
    {
        A get(String owner, String name);
    }

    /** Installed by the client (the lab runs without them, on reflection alone). */
    private static Table<ToDoubleFunction<Object>> vanillaNumbers = (owner, name) -> null;
    private static Table<Function<Object, Object>> vanillaParts = (owner, name) -> null;

    private DefinedFields()
    {
    }

    public static void install(Table<ToDoubleFunction<Object>> numbers, Table<Function<Object, Object>> parts)
    {
        vanillaNumbers = numbers;
        vanillaParts = parts;
    }

    /** A reader of the first numeric field among {@code candidates} that {@code type} or a superclass declares, or null. */
    public static ToDoubleFunction<Object> number(Class<?> type, List<String> candidates)
    {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass())
        {
            for (String name : candidates)
            {
                ToDoubleFunction<Object> generated = vanillaNumbers.get(c.getName(), name);
                if (generated != null)
                {
                    return generated;
                }
                Field field = declared(c, name);
                if (field != null && isNumeric(field.getType()))
                {
                    return instance -> {
                        try
                        {
                            return field.getDouble(instance);
                        }
                        catch (IllegalAccessException | IllegalArgumentException e)
                        {
                            return 0;
                        }
                    };
                }
            }
        }
        return null;
    }

    /** A reader of the field {@code name} that {@code type} or a superclass declares, or null. */
    public static Function<Object, Object> part(Class<?> type, String name)
    {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass())
        {
            Function<Object, Object> generated = vanillaParts.get(c.getName(), name);
            if (generated != null)
            {
                return generated;
            }
            Field field = declared(c, name);
            if (field != null && !field.getType().isPrimitive())
            {
                return instance -> {
                    try
                    {
                        return field.get(instance);
                    }
                    catch (IllegalAccessException | IllegalArgumentException e)
                    {
                        return null;
                    }
                };
            }
        }
        return null;
    }

    private static Field declared(Class<?> c, String name)
    {
        try
        {
            Field field = c.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        }
        catch (NoSuchFieldException | SecurityException e)
        {
            return null;
        }
    }

    private static boolean isNumeric(Class<?> type)
    {
        return type == float.class || type == double.class || type == int.class || type == long.class
                || type == short.class || type == byte.class;
    }

}
