package goblinbob.mobends.core.kumo.keyframe.node;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.INodeState;

@FunctionalInterface
public interface IKeyframeNodeFactory<D extends IEntityData, N extends INodeState<D>, T extends KeyframeNodeTemplate>
{
    N createKeyframeNode(IKumoInstancingContext<D> context, T template);
}
