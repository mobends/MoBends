package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.trigger.ITriggerCondition;

import java.util.List;

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

    public static <D extends IEntityData> ConnectionState<D> createFromTemplate(IKumoInstancingContext<D> context, List<INodeState<D>> nodes, ConnectionTemplate template) throws MalformedKumoTemplateException
    {
        INodeState<D> node = null;

        try
        {
            node = nodes.get(template.targetNodeIndex);
        }
        catch (IndexOutOfBoundsException ex)
        {
            throw new MalformedKumoTemplateException(String.format("A connection to node at index: %d was specified, which doesn't exist.",
                    template.targetNodeIndex));
        }

        if (template.triggerCondition == null)
        {
            throw new MalformedKumoTemplateException("No trigger condition was specified for a connection.");
        }

        return new ConnectionState(node,
                context.createTriggerCondition(template.triggerCondition, context),
                template.transitionDuration,
                template.transitionEasing);
    }
}