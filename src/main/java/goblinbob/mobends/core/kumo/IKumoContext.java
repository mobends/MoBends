package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.trigger.ITriggerConditionContext;

/**
 * This is a context which should provide all data necessary during the animation process.
 *
 * @author Iwo Plaza
 */
public interface IKumoContext<D extends IEntityData> extends ITriggerConditionContext<D>
{
    void setCurrentNode(INodeState<D> node);
}
