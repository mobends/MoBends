package net.gobbob.mobends.core.pack.state;

import net.gobbob.mobends.core.Core;
import net.gobbob.mobends.core.pack.BendsPackData;
import net.gobbob.mobends.core.pack.state.template.ConnectionTemplate;
import net.gobbob.mobends.core.pack.state.template.NodeTemplate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NodeState implements INodeState
{

    List<ConnectionState> connections = new ArrayList<>();
    List<ILayerState> layerStates = new LinkedList<>();

    public NodeState(BendsPackData data, NodeTemplate nodeTemplate)
    {
        for (NodeTemplate.LayerTemplateEntry entry : nodeTemplate.layers)
        {
            try
            {
                switch (entry.type)
                {
                    case KEYFRAME:
                        this.layerStates.add(KeyframeLayerState.createFromTemplate(data, data.stateTemplate.keyframeLayers.get(entry.index)));
                        break;
                    case DRIVER:
                        this.layerStates.add(new DriverLayerState(data.stateTemplate.driverLayers.get(entry.index)));
                        break;
                    default:
                        Core.LOG.warning(String.format("Unknown layer type was specified in state template: %d",
                                entry.type.ordinal()));
                }
            }
            catch (IndexOutOfBoundsException ex)
            {
                Core.LOG.warning(String.format("Malformed BendsPack state template!" +
                        "Node specified layer (%s) of index: %d, which doesn't exist.", entry.type.name(), entry.index));
            }
        }
    }

    public void parseConnections(List<INodeState> nodeStates, NodeTemplate template)
    {
        for (ConnectionTemplate connectionTemplate : template.connections)
        {
            try
            {
                this.connections.add(new ConnectionState(nodeStates.get(connectionTemplate.targetNodeIndex)));
            }
            catch (IndexOutOfBoundsException ex)
            {
                Core.LOG.warning(String.format("Malformed BendsPack state template!" +
                                "A connection to node at index: %d was specified, which doesn't exist.",
                        connectionTemplate.targetNodeIndex));
            }
        }
    }

    @Override
    public Iterable<ILayerState> getLayers()
    {
        return layerStates;
    }

}
