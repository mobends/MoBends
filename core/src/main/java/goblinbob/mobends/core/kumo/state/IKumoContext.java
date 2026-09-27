package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;

import java.util.Set;

/**
 * This is a context which should provide all data necessary during the animation process.
 *
 * @author Iwo Plaza
 */
public interface IKumoContext extends ITriggerConditionContext
{

    /** Ticks elapsed since the previous update. */
    float getDeltaTime();

    /** The node-local variable scope of the node being evaluated. Never null while a node is evaluated. */
    VariableScope getNodeScope();

    /** The variable scope of the layer being evaluated. Never null while a layer is evaluated. */
    VariableScope getLayerScope();

    /** Makes {@code node} the current node, with its own scope and its layer's {@code layerScope}. */
    void enterNode(INodeState node, VariableScope layerScope);

    /**
     * Makes the named variables read as their negation (see {@code MirroredPoseItem}).
     *
     * @return the previously negated set, to restore afterwards (null for none)
     */
    Set<String> setNegatedVariables(Set<String> names);

}
