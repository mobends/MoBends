package net.gobbob.mobends.core.pack.state;

import net.gobbob.mobends.core.pack.BendsPackData;
import net.gobbob.mobends.core.pack.state.template.NodeTemplate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PackAnimationState
{

    private BendsPackData bendsPackData;
    private List<INodeState> nodeStates;

    private void initFor(BendsPackData data)
    {
        this.bendsPackData = data;
        this.nodeStates = new ArrayList<>();
        for (NodeTemplate template : data.stateTemplate.nodes)
        {
            this.nodeStates.add(new NodeState(data, template));
        }

        for (int i = 0; i < this.nodeStates.size(); ++i)
        {
            this.nodeStates.get(i).parseConnections(this.nodeStates, data.stateTemplate.nodes.get(i));
        }
    }

    @Nullable
    public INodeState update(BendsPackData data, float deltaTime)
    {
        if (this.bendsPackData != data)
        {
            this.bendsPackData = data;
            this.initFor(data);
        }

        if (this.nodeStates.size() > 0)
        {
            INodeState state = this.nodeStates.get(0);
            for (ILayerState layer : state.getLayers())
            {
                layer.update(deltaTime);
            }
            return state;
        }

        return null;
    }

}
