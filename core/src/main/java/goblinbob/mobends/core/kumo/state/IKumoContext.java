package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;

/**
 * This is a context which should provide all data necessary during the animation process.
 *
 * @author Iwo Plaza
 */
public interface IKumoContext extends ITriggerConditionContext
{

    void setCurrentNode(INodeState node);

    /** Ticks elapsed since the previous update. */
    float getDeltaTime();

    /** The node-local variable scope of the node being evaluated. */
    VariableScope getNodeScope();

    /** The variable scope of the layer being evaluated. */
    VariableScope getLayerScope();

}
