package goblinbob.mobends.core.kumo.state.template.keyframe;

import goblinbob.mobends.core.kumo.state.IKumoValidationContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

/**
 * {@code "type": "core:vanilla"}: while a layer is in this node, the entity is drawn with its vanilla
 * model and vanilla animation, as if Mo' Bends didn't animate it. The animator keeps running
 * underneath (its connections are checked every frame, its other layers keep their clocks), so
 * leaving the node brings the animated model back where it would have been. The switch is
 * immediate either way: the two models can't be blended. It keeps the rest of a node: tags,
 * connections, {@code set}, {@code expressions}.
 */
public class VanillaNodeTemplate extends KeyframeNodeTemplate
{

    @Override
    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
        super.validate(context);
        if (damping != null || snapOnEnter != null || enterPose != null)
        {
            throw new MalformedKumoTemplateException(String.format("The vanilla node '%s' poses nothing: it can't have \"damping\", \"snapOnEnter\" or \"enterPose\".", name));
        }
    }

}
