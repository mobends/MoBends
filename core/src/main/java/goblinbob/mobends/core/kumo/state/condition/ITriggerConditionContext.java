package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.state.ILayerState;
import goblinbob.mobends.core.kumo.state.INodeState;

public interface ITriggerConditionContext
{

    /**
     * Returns the subject that's being animated.
     */
    IKumoSubject getSubject();

    /**
     * Returns the layer this condition has to be met on.
     */
    ILayerState getLayerState();

    /**
     * Returns the current node.
     */
    INodeState getCurrentNode();

    /**
     * @return true if any layer's current node carries the tag (layers before the one being
     *         evaluated already reflect this frame's transitions).
     */
    boolean isActionActive(String tag);

    /**
     * Resolves a variable through the scopes: node-local (ramps), layer variables, then the
     * subject. Throws if none has it.
     */
    double resolveVariable(String name);

    boolean hasVariable(String name);

}
