package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

@FunctionalInterface
public interface ITriggerCondition
{

    boolean isConditionMet(ITriggerConditionContext context) throws MalformedKumoTemplateException;

}
