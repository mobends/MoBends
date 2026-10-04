package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.TypeRegistry;
import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.AccumulateTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.AxisRotateTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.OffsetTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.SpringTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.StepTurnTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.VectorTemplate;

import javax.annotation.Nullable;

/**
 * Computed pose items ("drivers") addressable from animator JSON by key, e.g.
 * {@code {"core:axis_rotate": {...}}}. Addons register their own the same way
 * conditions are registered.
 */
public class DriverRegistry
{

    public static final DriverRegistry INSTANCE = new DriverRegistry();

    private final TypeRegistry<DriverItemTemplate, IDriverFactory<?>> registry = new TypeRegistry<>("driver");

    private DriverRegistry()
    {
        register("core:axis_rotate", AxisRotateDriver::create, AxisRotateTemplate.class);
        register("core:vector", VectorDriver::create, VectorTemplate.class);
        register("core:accumulate", AccumulateDriver::create, AccumulateTemplate.class);
        register("core:offset", OffsetDriver::create, OffsetTemplate.class);
        register("core:spring", SpringDriver::create, SpringTemplate.class);
        register("core:step_turn", StepTurnDriver::create, StepTurnTemplate.class);
    }

    public <T extends DriverItemTemplate> void register(String key, IDriverFactory<T> factory, Class<T> templateType)
    {
        registry.register(key, templateType, factory);
    }

    @Nullable
    public Class<? extends DriverItemTemplate> getTemplateClass(String key)
    {
        return registry.getTemplateClass(key);
    }

    @SuppressWarnings("unchecked")
    public <T extends DriverItemTemplate> IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, T template) throws MalformedKumoTemplateException
    {
        // The serializer read the template into the class registered under its key.
        IDriverFactory<T> factory = (IDriverFactory<T>) registry.getFactory(template.driver);
        return factory.create(context, skeleton, template);
    }

}
