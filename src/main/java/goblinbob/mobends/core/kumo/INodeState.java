package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.IEntityData;

import java.util.List;

public interface INodeState<D extends IEntityData>
{
    Iterable<ConnectionState<D>> getConnections();

    void parseConnections(List<? extends INodeState<D>> nodeStates, NodeTemplate template, IKumoInstancingContext<D> context);

    void start(IKumoContext<D> context);

    void update(IKumoContext<D> context);
}
