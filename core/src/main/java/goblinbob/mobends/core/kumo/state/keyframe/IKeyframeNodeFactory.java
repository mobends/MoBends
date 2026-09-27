package goblinbob.mobends.core.kumo.state.keyframe;

import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;

@FunctionalInterface
public interface IKeyframeNodeFactory<N extends INodeState, T extends KeyframeNodeTemplate>
{

    N createKeyframeNode(IKumoInstancingContext context, Skeleton skeleton, LayerTemplate layer, T template) throws MalformedKumoTemplateException;

}
