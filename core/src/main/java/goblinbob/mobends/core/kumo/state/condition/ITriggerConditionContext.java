package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.state.LayerState;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.VariableTable;

public interface ITriggerConditionContext
{

    /**
     * Returns the subject that's being animated.
     */
    IKumoSubject getSubject();

    /**
     * Returns the layer this condition has to be met on.
     */
    LayerState getLayerState();

    /**
     * Returns the current node.
     */
    INodeState getCurrentNode();


    /** The subject's variable {@code read}. */
    double resolveVariable(VariableTable.Read read);

    /** The number of the frame being evaluated: a live definition is computed once in each. */
    long getFrame();

    /** The state of the entity being animated (see {@link goblinbob.mobends.core.kumo.state.StateLayout}). */
    goblinbob.mobends.core.kumo.state.EntityState getState();

}
