package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.TypeRegistry;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

import javax.annotation.Nullable;

/** Trigger conditions addressable from animator JSON by {@code "type"}. */
public class TriggerConditionRegistry
{

    public static final TriggerConditionRegistry INSTANCE = new TriggerConditionRegistry();

    private final TypeRegistry<TriggerConditionTemplate, ITriggerConditionFactory<?, ?>> registry = new TypeRegistry<>("trigger condition");

    private TriggerConditionRegistry()
    {
        register("core:or", OrCondition::new, OrCondition.Template.class);
        register("core:and", AndCondition::new, AndCondition.Template.class);
        register("core:not", NotCondition::new, NotCondition.Template.class);
        register("core:state", StateCondition::new, StateCondition.Template.class);
        register("core:ticks_passed", (template, scope) -> new TicksPassedCondition(template), TicksPassedCondition.Template.class);
        register("core:compare", CompareCondition::new, CompareCondition.Template.class);
        register("core:action", (template, scope) -> new ActionCondition(template), ActionCondition.Template.class);
        register("core:property", (template, scope) -> new PropertyCondition(template), PropertyCondition.Template.class);
        register("core:decreased", DecreasedCondition::new, DecreasedCondition.Template.class);
        register(NamedCondition.TYPE, NamedCondition::create, NamedCondition.Template.class);
        register("core:animation_finished", context -> {
            INodeState node = context.getCurrentNode();
            return node != null && node.isAnimationFinished();
        });
    }

    public <T extends TriggerConditionTemplate> void register(String key, ITriggerConditionFactory<?, T> factory, Class<T> templateType)
    {
        registry.register(key, templateType, factory);
    }

    /** Registers a condition without parameters (and without state: the one instance is shared). */
    public void register(String key, ITriggerCondition condition)
    {
        registry.register(key, TriggerConditionTemplate.class, (ITriggerConditionFactory<ITriggerCondition, TriggerConditionTemplate>) (template, scope) -> condition);
    }

    @Nullable
    public Class<? extends TriggerConditionTemplate> getTemplateClass(String key)
    {
        return registry.getTemplateClass(key);
    }

    /** Creates the condition {@code template} describes, its expressions compiled against {@code scope}. */
    @SuppressWarnings("unchecked")
    public <T extends TriggerConditionTemplate> ITriggerCondition createFromTemplate(T template, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        // The serializer read the template into the class registered under its type.
        ITriggerConditionFactory<?, T> factory = (ITriggerConditionFactory<?, T>) registry.getFactory(template.getType());
        return factory.createTriggerCondition(template, scope);
    }

}
