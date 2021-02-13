package goblinbob.mobends.core.kumo.trigger;

import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.TriggerConditionTemplate;

@FunctionalInterface
public interface ITriggerConditionFactory<C extends ITriggerCondition, T extends TriggerConditionTemplate>
{

    C createTriggerCondition(T template) throws MalformedKumoTemplateException;

}
