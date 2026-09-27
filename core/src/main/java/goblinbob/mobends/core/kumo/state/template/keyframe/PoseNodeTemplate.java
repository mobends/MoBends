package goblinbob.mobends.core.kumo.state.template.keyframe;

import goblinbob.mobends.core.kumo.state.IKumoValidationContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.ClipItemTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.PoseItemTemplate;

import java.util.List;

/**
 * Format 2 node ({@code "type": "core:pose"}): an ordered stack of clips and drivers that
 * together form the pose.
 */
public class PoseNodeTemplate extends KeyframeNodeTemplate
{

    public List<PoseItemTemplate> pose;

    @Override
    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
        super.validate(context);

        if (pose != null)
        {
            for (PoseItemTemplate item : pose)
            {
                if (item instanceof ClipItemTemplate)
                {
                    String key = ((ClipItemTemplate) item).animationKey;
                    if (!context.doesAnimationExist(key))
                    {
                        throw new MalformedKumoTemplateException(String.format("Trying to use a missing animation: \"%s\".", key));
                    }
                }
            }
        }
    }

}
