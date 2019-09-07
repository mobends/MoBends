package net.gobbob.mobends.core.pack.state;

import net.gobbob.mobends.core.Core;
import net.gobbob.mobends.core.pack.BendsPackData;
import net.gobbob.mobends.core.pack.state.template.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NodeState implements INodeState
{

    List<ConnectionState> connections = new ArrayList<>();
    List<ILayerState> layerStates = new LinkedList<>();

    public NodeState(BendsPackData data, NodeTemplate nodeTemplate)
    {
        if (nodeTemplate.layers == null)
            return;

        for (LayerTemplate template : nodeTemplate.layers)
        {
            switch (template.getLayerType())
            {
                case KEYFRAME:
                    this.layerStates.add(KeyframeLayerState.createFromTemplate(data, (KeyframeLayerTemplate) template));
                    break;
                case DRIVER:
                    this.layerStates.add(new DriverLayerState((DriverLayerTemplate) template));
                    break;
                default:
                    Core.LOG.warning(String.format("Unknown layer type was specified in state template: %d",
                            template.getLayerType().ordinal()));
            }
        }
    }

    public void parseConnections(List<INodeState> nodeStates, NodeTemplate template) throws MalformedPackTemplateException
    {
        if (template.connections != null)
        {
            for (ConnectionTemplate connectionTemplate : template.connections)
            {
                this.connections.add(ConnectionState.createFromTemplate(nodeStates, connectionTemplate));
            }
        }
    }

    @Override
    public void start()
    {
        for (ILayerState state : layerStates)
        {
            state.start();
        }
    }

    @Override
    public Iterable<ConnectionState> getConnections()
    {
        return connections;
    }

    @Override
    public Iterable<ILayerState> getLayers()
    {
        return layerStates;
    }

}
