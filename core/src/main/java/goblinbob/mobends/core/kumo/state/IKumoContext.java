package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;


/**
 * This is a context which should provide all data necessary during the animation process.
 *
 * @author Iwo Plaza
 */
public interface IKumoContext extends ITriggerConditionContext
{

    /** Ticks elapsed since the previous update. */
    float getDeltaTime();

    /** Makes {@code node} the node being evaluated. */
    void enterNode(INodeState node);

}
