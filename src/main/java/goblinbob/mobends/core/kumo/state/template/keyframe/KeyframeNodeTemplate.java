package goblinbob.mobends.core.kumo.state.template.keyframe;

import goblinbob.mobends.core.kumo.state.IKumoValidationContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.List;

public class KeyframeNodeTemplate
{

    private String type = "core:standard";

    public List<ConnectionTemplate> connections;

    public String getType()
    {
        return type;
    }

    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
        for (ConnectionTemplate connectionTemplate : connections)
        {
            connectionTemplate.validate(context);
        }
    }

}
