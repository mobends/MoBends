package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.state.IKumoValidationContext;

public class TriggerConditionTemplate
{

    public String type;

    public String getType()
    {
        return type;
    }

    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
        // No default behaviour.
    }

}
