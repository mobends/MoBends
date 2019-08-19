package net.gobbob.mobends.core.pack.state.condition;

import net.gobbob.mobends.core.pack.state.template.TriggerConditionTemplate;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TriggerConditionRegistry
{

    public static final TriggerConditionRegistry instance = new TriggerConditionRegistry();

    private Map<String, ITriggerConditionFactory<?, ?>> registry = new HashMap<>();

    private TriggerConditionRegistry()
    {
        register("core:or", OrCondition::new, OrCondition.Template::new);
    }

    public <T extends TriggerConditionTemplate> void register(String key, ITriggerConditionFactory<?, T> factory)
    {
        registry.put(key, factory);
    }

    @Nullable
    public ITriggerCondition createFromTemplate(TriggerConditionTemplate template)
    {
        if (!registry.containsKey(template.getType()))
            return null;

        final ITriggerConditionFactory<?> t = registry.get(template.getType());
        return t.createTriggerCondition(template);
    }

    private static class RegistryEntry<T extends TriggerConditionTemplate>
    {

        ITriggerConditionFactory<?, T> factory;
        Type templateType;

    }

}
