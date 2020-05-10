package goblinbob.mobends.core.kumo.state.keyframe;

import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;

@FunctionalInterface
public interface IKeyframeNodeFactory<N extends INodeState, T extends KeyframeNodeTemplate>
{

    N createKeyframeNode(IKumoInstancingContext context, T template) throws MalformedKumoTemplateException;

}
