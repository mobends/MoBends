package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

@FunctionalInterface
public interface ITriggerConditionFactory<C extends ITriggerCondition, T extends TriggerConditionTemplate>
{

    /**
     * @param scope the named expressions visible where the condition is written, for compiling the
     *              expressions it takes and for the conditions nested in it
     */
    C createTriggerCondition(T template, ExpressionScope scope) throws MalformedKumoTemplateException;

}
