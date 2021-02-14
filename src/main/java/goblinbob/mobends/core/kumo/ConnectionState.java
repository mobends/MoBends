package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.trigger.ITriggerCondition;

public class ConnectionState<D extends IEntityData>
{
    public final INodeState<D> targetNode;
    public final ITriggerCondition<D> triggerCondition;
    public final float transitionDuration;
    public ConnectionTemplate.Easing transitionEasing;

    public ConnectionState(INodeState<D> targetNode, ITriggerCondition<D> triggerCondition, float transitionDuration, ConnectionTemplate.Easing transitionEasing)
    {
        this.targetNode = targetNode;
        this.triggerCondition = triggerCondition;
        this.transitionDuration = transitionDuration;
        this.transitionEasing = transitionEasing;
    }
}