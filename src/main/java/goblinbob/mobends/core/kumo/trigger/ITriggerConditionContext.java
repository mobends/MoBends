package goblinbob.mobends.core.kumo.trigger;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.ILayerState;
import goblinbob.mobends.core.kumo.INodeState;

public interface ITriggerConditionContext<D extends IEntityData>
{
    /**
     * Ticks passed since the start of the world.
     * @return
     */
    float getTicksPassed();

    /**
     * Returns data for the entity that's being animated.
     */
    D getEntityData();

    /**
     * Returns the layer this condition has to be met on.
     */
    ILayerState getLayerState();

    /**
     * Returns the current node.
     */
    INodeState getCurrentNode();
}
