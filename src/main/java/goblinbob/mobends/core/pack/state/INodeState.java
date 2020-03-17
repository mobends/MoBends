package goblinbob.mobends.core.pack.state;

import goblinbob.mobends.core.pack.state.template.MalformedPackTemplateException;
import goblinbob.mobends.core.pack.state.template.NodeTemplate;

import java.util.List;

public interface INodeState
{

    void parseConnections(List<INodeState> nodeStates, NodeTemplate template) throws MalformedPackTemplateException;

    Iterable<ConnectionState> getConnections();

    Iterable<ILayerState> getLayers();

    void start();

}
