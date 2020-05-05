package goblinbob.mobends.core.animation.layer;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.INodeState;

import java.util.ArrayList;
import java.util.List;

public class NodeAnimationLayer<T extends EntityData<?>> extends AnimationLayer<T>
{

    private final List<INodeState> nodeStates = new ArrayList<>();
    private INodeState currentNode;

    @Override
    public String[] getActions(T entityData)
    {
        return new String[0];
    }

    @Override
    public void perform(T entityData)
    {

    }

}
