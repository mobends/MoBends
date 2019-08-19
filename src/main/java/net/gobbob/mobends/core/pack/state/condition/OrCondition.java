package net.gobbob.mobends.core.pack.state.condition;

import net.gobbob.mobends.core.pack.state.template.TriggerConditionTemplate;

public class OrCondition implements ITriggerCondition
{

    public OrCondition(Template template)
    {

    }

    @Override
    public boolean isConditionMet()
    {
        return false;
    }

    public static class Template extends TriggerConditionTemplate
    {



    }

}
