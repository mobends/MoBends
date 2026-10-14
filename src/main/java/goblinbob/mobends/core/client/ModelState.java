package goblinbob.mobends.core.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What a mutation changes about a model: its part fields, and the contents of its part arrays and
 * lists, captured so they can be put back in place.
 */
public final class ModelState
{

    private static final Map<Class<?>, List<Field>> FIELDS_BY_MODEL_CLASS = new HashMap<>();

    private final ModelBase model;
    private final List<Field> fields;
    private final Object[] values;
    /** Per field: a copy of the array's or list's elements, or null for a plain part. */
    private final Object[][] elements;

    private ModelState(ModelBase model)
    {
        this.model = model;
        this.fields = partFields(model.getClass());
        this.values = new Object[fields.size()];
        this.elements = new Object[fields.size()][];
        for (int i = 0; i < fields.size(); i++)
        {
            Object value = read(fields.get(i), model);
            values[i] = value;
            if (value instanceof Object[])
            {
                elements[i] = ((Object[]) value).clone();
            }
            else if (value instanceof List)
            {
                elements[i] = ((List<?>) value).toArray();
            }
        }
    }

    /** The model's state as it is now. */
    public static ModelState capture(ModelBase model)
    {
        return new ModelState(model);
    }

    public ModelBase getModel()
    {
        return model;
    }

    /** Puts this state back in place on its model. */
    @SuppressWarnings("unchecked")
    public void apply()
    {
        for (int i = 0; i < fields.size(); i++)
        {
            Field field = fields.get(i);
            Object value = values[i];
            if (read(field, model) != value)
            {
                write(field, model, value);
            }
            if (elements[i] == null)
            {
                continue;
            }
            if (value instanceof Object[])
            {
                System.arraycopy(elements[i], 0, value, 0, elements[i].length);
            }
            else if (value instanceof List)
            {
                List<Object> list = (List<Object>) value;
                list.clear();
                Collections.addAll(list, elements[i]);
            }
        }
    }

    /** Every instance field of the model's class hierarchy that holds a part, an array of parts or a list. */
    private static List<Field> partFields(Class<?> modelClass)
    {
        return FIELDS_BY_MODEL_CLASS.computeIfAbsent(modelClass, c -> {
            List<Field> found = new ArrayList<>();
            for (Class<?> type = c; type != null && type != Object.class; type = type.getSuperclass())
            {
                for (Field field : type.getDeclaredFields())
                {
                    if (Modifier.isStatic(field.getModifiers()) || !holdsParts(field.getType()))
                    {
                        continue;
                    }
                    try
                    {
                        field.setAccessible(true);
                        found.add(field);
                    }
                    catch (SecurityException ignored)
                    {
                    }
                }
            }
            return found;
        });
    }

    private static boolean holdsParts(Class<?> type)
    {
        if (ModelRenderer.class.isAssignableFrom(type) || List.class.isAssignableFrom(type))
        {
            return true;
        }
        if (type.isArray())
        {
            Class<?> component = type.getComponentType();
            return ModelRenderer.class.isAssignableFrom(component) || component == Object.class;
        }
        return false;
    }

    private static Object read(Field field, Object holder)
    {
        try
        {
            return field.get(holder);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static void write(Field field, Object holder, Object value)
    {
        try
        {
            field.set(holder, value);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("Cannot restore " + field, e);
        }
    }

}
