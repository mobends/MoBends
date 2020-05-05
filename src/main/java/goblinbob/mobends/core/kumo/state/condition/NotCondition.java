package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

public class NotCondition implements ITriggerCondition
{

    private ITriggerCondition condition;

    public NotCondition(Template template) throws MalformedKumoTemplateException
    {
        this.condition = TriggerConditionRegistry.instance.createFromTemplate(template.condition);
    }

    public boolean isConditionMet(EntityData<?> entityData)
    {
        return !this.condition.isConditionMet(entityData);
    }

    public static class Template extends TriggerConditionTemplate
    {

        public TriggerConditionTemplate condition;

    }

}
