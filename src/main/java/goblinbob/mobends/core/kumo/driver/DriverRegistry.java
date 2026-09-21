package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.AxisRotateTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Procedural pose items ("drivers") addressable from animator JSON by key, e.g.
 * {@code "driver": "core:axis_rotate"}. Addons register their own the same way trigger
 * conditions are registered.
 */
public class DriverRegistry
{

    public static final DriverRegistry INSTANCE = new DriverRegistry();

    private final Map<String, Entry<?>> registry = new HashMap<>();

    private DriverRegistry()
    {
        register("core:axis_rotate", AxisRotateDriver::create, AxisRotateTemplate.class);
    }

    public <T extends DriverItemTemplate> void register(String key, IDriverFactory<T> factory, Class<T> templateType)
    {
        registry.put(key, new Entry<T>(factory, templateType));
    }

    @Nullable
    public Type getTemplateClass(String key)
    {
        Entry<?> entry = registry.get(key);
        return entry == null ? null : entry.templateType;
    }

    @SuppressWarnings("unchecked")
    public <T extends DriverItemTemplate> IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, T template) throws MalformedKumoTemplateException
    {
        if (template.driver == null)
        {
            throw new MalformedKumoTemplateException("A pose item has neither 'animationKey' nor 'driver'.");
        }
        Entry<T> entry = (Entry<T>) registry.get(template.driver);
        if (entry == null)
        {
            throw new MalformedKumoTemplateException(String.format("Unknown driver: '%s'", template.driver));
        }
        if (!entry.templateType.equals(template.getClass()))
        {
            throw new MalformedKumoTemplateException(String.format("The driver registry holds a wrong entry for '%s'", template.driver));
        }
        return entry.factory.create(context, skeleton, template);
    }

    private static class Entry<T extends DriverItemTemplate>
    {
        final IDriverFactory<T> factory;
        final Class<T> templateType;

        Entry(IDriverFactory<T> factory, Class<T> templateType)
        {
            this.factory = factory;
            this.templateType = templateType;
        }
    }

}
