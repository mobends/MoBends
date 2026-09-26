package goblinbob.mobends.core.kumo.state.template.keyframe;

import goblinbob.mobends.core.kumo.state.IKumoValidationContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

/**
 * {@code "type": "core:fallthrough"}: a node that poses nothing, so the layers below show
 * through. A transition into or out of it fades between the layer's pose and theirs. It keeps
 * the rest of a node: tags, connections, {@code set}, {@code expressions}.
 */
public class FallthroughNodeTemplate extends KeyframeNodeTemplate
{

    @Override
    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
        super.validate(context);
        if (damping != null || snapOnEnter != null || enterPose != null)
        {
            throw new MalformedKumoTemplateException(String.format("The fallthrough node '%s' poses nothing: it can't have \"damping\", \"snapOnEnter\" or \"enterPose\".", name));
        }
    }

}
