package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.state.ILayerState;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TriggerConditionRegistry
{

    public static final TriggerConditionRegistry instance = new TriggerConditionRegistry();

    private Map<String, RegistryEntry<?>> registry = new HashMap<>();
    private Map<String, ITriggerCondition> pureRegistry = new HashMap<>();

    private TriggerConditionRegistry()
    {
        register("core:or", OrCondition::new, OrCondition.Template.class);
        register("core:not", NotCondition::new, NotCondition.Template.class);
        register("core:state", StateCondition::new, StateCondition.Template.class);
        register("core:animation_finished", (context) -> {
            INodeState node = context.getCurrentNode();
            if (node != null)
            {
                return node.isAnimationFinished();
            }
            return false;
        });
    }

    public <T extends TriggerConditionTemplate> void register(String key, ITriggerConditionFactory<?, T> factory, Class<T> templateType)
    {
        registry.put(key, new RegistryEntry<T>(factory, templateType));
    }

    public void register(String key, ITriggerCondition condition)
    {
        pureRegistry.put(key, condition);
    }

    @Nullable
    public Type getTemplateClass(String key)
    {
        if (registry.containsKey(key))
        {
            return registry.get(key).templateType;
        }
        if (pureRegistry.containsKey(key))
        {
            return TriggerConditionTemplate.class;
        }
        return null;
    }

    public <T extends TriggerConditionTemplate> ITriggerCondition createFromTemplate(T template) throws MalformedKumoTemplateException
    {
        final String type = template.getType();

        if (type == null)
        {
            throw new MalformedKumoTemplateException("No type was specified for trigger condition.");
        }

        if (registry.containsKey(type))
        {
            @SuppressWarnings("unchecked") final RegistryEntry<T> entry = (RegistryEntry<T>) registry.get(type);
            return createFromTemplate(entry, template);
        }
        else if (pureRegistry.containsKey(type))
        {
            return pureRegistry.get(type);
        }
        else
        {
            throw new MalformedKumoTemplateException(String.format("A non-existent trigger condition type was specified: %s", type));
        }
    }

    private <T extends TriggerConditionTemplate> ITriggerCondition createFromTemplate(RegistryEntry<T> entry, T template) throws MalformedKumoTemplateException
    {
        if (!entry.templateType.equals(template.getClass()))
        {
            throw new MalformedKumoTemplateException(String.format("The trigger condition registry holds a wrong entry for '%s'", template.getType()));
        }

        return entry.factory.createTriggerCondition(template);
    }

    private static class RegistryEntry<T extends TriggerConditionTemplate>
    {

        public ITriggerConditionFactory<?, T> factory;
        public Class<T> templateType;

        RegistryEntry(ITriggerConditionFactory<?, T> factory, Class<T> templateType)
        {
            this.factory = factory;
            this.templateType = templateType;
        }

    }

}
