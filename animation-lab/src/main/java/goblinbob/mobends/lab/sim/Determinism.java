package goblinbob.mobends.lab.sim;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * The mod uses unseeded {@link Random} instances for variation (e.g. the zombie walking state).
 * The lab replaces them with seeded ones so every scenario replays identically.
 */
public class Determinism
{
    public static void seedRandoms(Object target, long seed)
    {
        Class<?> c = target.getClass();
        while (c != null && c != Object.class)
        {
            for (Field field : c.getDeclaredFields())
            {
                if (field.getType() == Random.class && !java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                {
                    try
                    {
                        field.setAccessible(true);
                        field.set(target, new Random(seed ^ field.getName().hashCode()));
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new IllegalStateException("cannot seed " + c.getName() + "." + field.getName(), e);
                    }
                }
            }
            c = c.getSuperclass();
        }
    }
}
