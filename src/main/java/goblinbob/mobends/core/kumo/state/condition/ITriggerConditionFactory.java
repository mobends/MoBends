package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

@FunctionalInterface
public interface ITriggerConditionFactory<C extends ITriggerCondition, T extends TriggerConditionTemplate>
{

    C createTriggerCondition(T template) throws MalformedKumoTemplateException;

}
