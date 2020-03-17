package goblinbob.mobends.core.pack.state;

import goblinbob.mobends.core.pack.state.condition.ITriggerCondition;
import goblinbob.mobends.core.pack.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.pack.state.template.ConnectionTemplate;
import goblinbob.mobends.core.pack.state.template.MalformedPackTemplateException;

import java.util.List;

public class ConnectionState
{

    public final INodeState targetNode;
    public final ITriggerCondition triggerCondition;

    public ConnectionState(INodeState targetNode, ITriggerCondition triggerCondition)
    {
        this.targetNode = targetNode;
        this.triggerCondition = triggerCondition;
    }

    public static ConnectionState createFromTemplate(List<INodeState> nodes, ConnectionTemplate template) throws MalformedPackTemplateException
    {
        INodeState node = null;

        try
        {
            node = nodes.get(template.targetNodeIndex);
        }
        catch (IndexOutOfBoundsException ex)
        {
            throw new MalformedPackTemplateException(String.format("A connection to node at index: %d was specified, which doesn't exist.",
                    template.targetNodeIndex));
        }

        if (template.triggerCondition == null)
        {
            throw new MalformedPackTemplateException("No trigger condition was specified for a connection.");
        }

        return new ConnectionState(node, TriggerConditionRegistry.instance.createFromTemplate(template.triggerCondition));
    }

}
