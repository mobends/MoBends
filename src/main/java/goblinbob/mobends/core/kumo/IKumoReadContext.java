package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.IEntityData;

public interface IKumoReadContext<D extends IEntityData>
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
    ILayerState<D> getLayerState();

    /**
     * Returns the current node.
     */
    INodeState<D> getCurrentNode();
}
