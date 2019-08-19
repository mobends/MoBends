package net.gobbob.mobends.core.pack.state.condition;

import net.gobbob.mobends.core.pack.state.template.TriggerConditionTemplate;

@FunctionalInterface
public interface ITriggerConditionFactory<C extends ITriggerCondition, T extends TriggerConditionTemplate>
{

    C createTriggerCondition(T template);

}
