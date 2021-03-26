package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.IEntityData;

import java.util.ArrayList;
import java.util.List;

public abstract class NodeState<D extends IEntityData> implements INodeState<D>
{
    protected List<ConnectionState<D>> connections = new ArrayList<>();

    @Override
    public void parseConnections(List<? extends INodeState<D>> nodeStates, NodeTemplate template, IKumoInstancingContext<D> context)
    {
        if (template.connections != null)
        {
            for (ConnectionTemplate connectionTemplate : template.connections)
            {
                this.connections.add(connectionTemplate.instantiate(nodeStates, context));
            }
        }
    }

    @Override
    public Iterable<ConnectionState<D>> getConnections()
    {
        return connections;
    }
}
