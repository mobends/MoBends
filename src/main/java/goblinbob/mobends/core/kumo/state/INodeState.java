package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.NodeTemplate;

import java.util.List;

public interface INodeState
{

    void parseConnections(List<INodeState> nodeStates, NodeTemplate template) throws MalformedKumoTemplateException;

    Iterable<ConnectionState> getConnections();

    Iterable<ILayerState> getLayers();

    void start();

}
