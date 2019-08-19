package net.gobbob.mobends.core.pack.state.condition;

import net.gobbob.mobends.core.data.EntityData;
import net.gobbob.mobends.core.pack.state.template.MalformedPackTemplateException;
import net.gobbob.mobends.core.pack.state.template.TriggerConditionTemplate;

public class NotCondition implements ITriggerCondition
{

    private ITriggerCondition condition;

    public NotCondition(Template template) throws MalformedPackTemplateException
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
