package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.node.NodeRegistry;
import goblinbob.mobends.core.kumo.state.template.ConnectionTemplate;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MachineTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.NodeTemplate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A machine of a layer (the layer's own, or one nested in it): its nodes and machines, the selector
 * that chooses between them and the connections out of any node inside it.
 */
public class MachineState
{

    /** The machine's name; null for a layer's own. */
    @Nullable
    public final String name;
    /** Where the machine sits in the machine around it; null for a layer's own. */
    @Nullable
    MachineMember member;
    final List<MachineMember> members = new ArrayList<>();
    /** Where the machine goes when it is entered and its selector chooses nothing. */
    MachineMember defaultOnEntry;
    @Nullable
    Selector selector;
    final List<ConnectionState> connections = new ArrayList<>();

    private final MachineTemplate template;
    private final ExpressionScope scope;
    /** Per node member: its template and the scope its connections see, for {@link #link}. */
    private final List<NodeTemplate> nodeTemplates = new ArrayList<>();
    private final List<ExpressionScope> nodeScopes = new ArrayList<>();

    /**
     * Instances the machine's nodes and machines, and registers them by name; {@link #link} then
     * creates the selectors and connections, once every name of the layer is known.
     *
     * @param context the context of the machine's own declarations
     */
    MachineState(IKumoInstancingContext context, Skeleton skeleton, LayerTemplate layer, MachineTemplate template, Map<String, MachineMember> membersByName) throws MalformedKumoTemplateException
    {
        this.name = template.name;
        this.template = template;
        this.scope = context.getExpressionScope();

        List<NodeTemplate> nodes = template.nodes == null ? Collections.<NodeTemplate>emptyList() : template.nodes;
        List<MachineTemplate> machines = template.machines == null ? Collections.<MachineTemplate>emptyList() : template.machines;
        if (nodes.isEmpty() && machines.isEmpty())
        {
            throw new MalformedKumoTemplateException(name == null ? "A layer has no nodes." : String.format("The machine '%s' has no nodes.", name));
        }

        for (NodeTemplate nodeTemplate : nodes)
        {
            IKumoInstancingContext nodeContext = context.withDeclarations(nodeTemplate.expressions, nodeTemplate.conditions);
            INodeState node = NodeRegistry.INSTANCE.createFromTemplate(nodeContext, skeleton, layer, nodeTemplate);
            register(membersByName, new MachineMember(nodeTemplate.name, node, null, this));
            nodeTemplates.add(nodeTemplate);
            nodeScopes.add(nodeContext.getExpressionScope());
        }
        for (MachineTemplate machineTemplate : machines)
        {
            if (machineTemplate == null)
            {
                throw new MalformedKumoTemplateException(String.format("%s has a null machine.", describe()));
            }
            IKumoInstancingContext machineContext = context.withDeclarations(machineTemplate.expressions, machineTemplate.conditions);
            MachineState machine = new MachineState(machineContext, skeleton, layer, machineTemplate, membersByName);
            machine.member = new MachineMember(machineTemplate.name, null, machine, this);
            register(membersByName, machine.member);
        }
    }

    private void register(Map<String, MachineMember> membersByName, MachineMember member) throws MalformedKumoTemplateException
    {
        if (membersByName.put(member.name, member) != null)
        {
            throw new MalformedKumoTemplateException(String.format("Two nodes or machines of the layer share the name '%s'.", member.name));
        }
        members.add(member);
    }

    /** Creates the selectors, connections and entries of this machine and of the machines inside it. */
    void link(Map<String, MachineMember> membersByName) throws MalformedKumoTemplateException
    {
        for (int i = 0; i < nodeTemplates.size(); i++)
        {
            List<ConnectionTemplate> nodeConnections = nodeTemplates.get(i).connections;
            if (nodeConnections != null)
            {
                for (ConnectionTemplate connection : nodeConnections)
                {
                    members.get(i).connections.add(ConnectionState.createFromTemplate(membersByName, connection, nodeScopes.get(i)));
                }
            }
        }
        if (template.connections != null)
        {
            for (ConnectionTemplate connection : template.connections)
            {
                connections.add(ConnectionState.createFromTemplate(membersByName, connection, scope));
            }
        }
        if (template.select != null)
        {
            selector = Selector.create(template.select, this, membersByName, scope);
        }

        if (template.defaultOnEntry != null)
        {
            defaultOnEntry = membersByName.get(template.defaultOnEntry);
            if (defaultOnEntry == null || defaultOnEntry.parent != this)
            {
                throw new MalformedKumoTemplateException(String.format("The defaultOnEntry '%s' of %s isn't one of its own nodes or machines.", template.defaultOnEntry, describe()));
            }
        }
        else
        {
            // The first node, or the first machine if there are no nodes (nodes come first in the list).
            defaultOnEntry = members.get(0);
        }

        for (MachineMember member : members)
        {
            if (member.machine != null)
            {
                member.machine.link(membersByName);
            }
        }
    }

    /**
     * The node reached by following the defaultOnEntry of each machine on the way, selectors aside:
     * where a layer stands before it starts (see {@link LayerState#start}).
     */
    MachineMember initialNode()
    {
        MachineMember member = defaultOnEntry;
        while (member.machine != null)
        {
            member = member.machine.defaultOnEntry;
        }
        return member;
    }

    /** Starts the conditions of the selector and connections over: the machine was entered. */
    void start(ITriggerConditionContext context)
    {
        if (selector != null)
        {
            selector.start(context);
        }
        for (ConnectionState connection : connections)
        {
            connection.triggerCondition.onNodeStarted(context);
        }
    }

    String describe()
    {
        return name == null ? "the layer" : String.format("the machine '%s'", name);
    }

}
