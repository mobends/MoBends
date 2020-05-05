package goblinbob.mobends.core.pack.state.condition;

import goblinbob.mobends.core.pack.state.template.MalformedPackTemplateException;
import goblinbob.mobends.core.pack.state.template.TriggerConditionTemplate;

@FunctionalInterface
public interface ITriggerConditionFactory<C extends ITriggerCondition, T extends TriggerConditionTemplate>
{

    C createTriggerCondition(T template) throws MalformedPackTemplateException;

}
