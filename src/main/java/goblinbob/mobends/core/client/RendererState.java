package goblinbob.mobends.core.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * What a mutation changes about a renderer: the model's part fields, the contents of its part
 * arrays and lists, and the renderer's layers. One renderer draws every entity of a kind (every
 * player is drawn by one of two), and entities that share it can have different types, so a
 * renderer's vanilla state and each of its mutations are captured once and put back in place
 * before every render, instead of mutating and demutating.
 */
public final class RendererState
{

    private static final Map<Class<?>, List<Field>> FIELDS_BY_MODEL_CLASS = new HashMap<>();
    private static final Map<RenderLivingBase<?>, RendererState> VANILLA = new WeakHashMap<>();
    /** Renderers that currently hold a mutation. */
    private static final Map<RenderLivingBase<?>, Boolean> MUTATED = new WeakHashMap<>();

    private final ModelBase model;
    private final List<Field> fields;
    private final Object[] values;
    /** Per field: a copy of the array's or list's elements, or null for a plain part. */
    private final Object[][] elements;
    private final List<LayerRenderer<?>> layers;

    private RendererState(RenderLivingBase<?> renderer)
    {
        this.model = renderer.getMainModel();
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
        this.layers = new ArrayList<>(renderer.layerRenderers);
    }

    /** The renderer's state as it is now. */
    public static RendererState capture(RenderLivingBase<?> renderer)
    {
        return new RendererState(renderer);
    }

    /** Puts the renderer back to vanilla, if a mutation is in place. */
    public static void restoreVanilla(RenderLivingBase<?> renderer)
    {
        if (MUTATED.remove(renderer) != null)
        {
            vanillaOf(renderer).apply(renderer);
        }
    }

    /**
     * The renderer's vanilla state, captured the first time it is asked for. Every mutation goes
     * through {@link #restoreVanilla} first, so this is always seen before the first mutation.
     */
    public static RendererState vanillaOf(RenderLivingBase<?> renderer)
    {
        return VANILLA.computeIfAbsent(renderer, RendererState::new);
    }

    /** Puts this mutation in place on the renderer. */
    public void applyMutation(RenderLivingBase<?> renderer)
    {
        vanillaOf(renderer);
        apply(renderer);
        MUTATED.put(renderer, Boolean.TRUE);
    }

    /** Marks the renderer as mutated, after a mutator changed it directly. */
    public static void markMutated(RenderLivingBase<?> renderer)
    {
        MUTATED.put(renderer, Boolean.TRUE);
    }

    /** Forgets every captured state (the next render captures vanilla anew). */
    public static void forgetAll()
    {
        VANILLA.clear();
        MUTATED.clear();
    }

    @SuppressWarnings("unchecked")
    private void apply(RenderLivingBase<?> renderer)
    {
        if (renderer.getMainModel() != model)
        {
            // Another mod replaced the model; the captured parts no longer belong to it.
            return;
        }
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
        List<LayerRenderer<?>> rendererLayers = (List<LayerRenderer<?>>) (Object) renderer.layerRenderers;
        rendererLayers.clear();
        rendererLayers.addAll(layers);
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
