package goblinbob.mobends.core.kumo.trigger;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;

@FunctionalInterface
public interface ITriggerConditionFactory<D extends IEntityData, C extends ITriggerCondition<D>, T extends TriggerConditionTemplate>
{
    C createTriggerCondition(T template, IKumoInstancingContext<D> context) throws MalformedKumoTemplateException;
}
