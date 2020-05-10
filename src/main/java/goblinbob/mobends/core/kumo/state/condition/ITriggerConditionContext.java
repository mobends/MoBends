package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.ILayerState;
import goblinbob.mobends.core.kumo.state.INodeState;

public interface ITriggerConditionContext
{

    /**
     * Returns data for the entity that's being animated.
     */
    EntityData<?> getEntityData();

    /**
     * Returns the layer this condition has to be met on.
     */
    ILayerState getLayerState();

    /**
     * Returns the current node.
     */
    INodeState getCurrentNode();

}
