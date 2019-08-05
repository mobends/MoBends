package net.gobbob.mobends.core.pack.state;

import net.gobbob.mobends.core.pack.state.template.NodeTemplate;

import java.util.List;

public interface INodeState
{

    void parseConnections(List<INodeState> nodeStates, NodeTemplate template);

    Iterable<ILayerState> getLayers();

}
