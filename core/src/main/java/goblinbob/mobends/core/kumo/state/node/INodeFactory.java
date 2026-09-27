package goblinbob.mobends.core.kumo.state.node;

import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.NodeTemplate;

@FunctionalInterface
public interface INodeFactory<N extends INodeState, T extends NodeTemplate>
{

    N createNode(IKumoInstancingContext context, Skeleton skeleton, LayerTemplate layer, T template) throws MalformedKumoTemplateException;

}
