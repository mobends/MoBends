package goblinbob.mobends.core.kumo.state.template.keyframe;

import goblinbob.mobends.core.animation.keyframe.ArmatureMask;
import goblinbob.mobends.core.kumo.state.IKumoValidationContext;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.List;

public class KeyframeLayerTemplate extends LayerTemplate
{

    /** Name of the entry node (JSON: {@code entryNode}). */
    public String entryNodeName;

    public List<KeyframeNodeTemplate> nodes;

    public ArmatureMask mask;

    @Override
    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
        super.validate(context);

        if (nodes == null || nodes.isEmpty())
        {
            throw new MalformedKumoTemplateException("A keyframe layer has no nodes.");
        }

        for (KeyframeNodeTemplate node : nodes)
        {
            node.validate(context);
        }
    }

}
