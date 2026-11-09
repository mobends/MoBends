package goblinbob.mobends.lab.sim;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * The mod uses unseeded {@link Random} instances for variation (e.g. the zombie walking state).
 * The lab seeds them so every scenario replays identically.
 */
public class Determinism
{
    /** Sets a (possibly protected) field by name, searching superclasses. */
    public static void setField(Object target, String name, Object value)
    {
        Class<?> c = target.getClass();
        while (c != null)
        {
            try
            {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                f.set(target, value);
                return;
            }
            catch (NoSuchFieldException e)
            {
                c = c.getSuperclass();
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalArgumentException("no field " + name + " on " + target.getClass());
    }

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
                        long fieldSeed = seed ^ field.getName().hashCode();
                        Random random = (Random) field.get(target);
                        // Reseeded in place: what reads it may hold it already (the data's
                        // "random" built-in is bound to it as the data is made).
                        if (random != null)
                        {
                            random.setSeed(fieldSeed);
                        }
                        else
                        {
                            field.set(target, new Random(fieldSeed));
                        }
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
