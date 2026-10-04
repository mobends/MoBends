package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A machine: nodes and nested machines, the selector that chooses between them and the connections
 * that lead out of any of them. A layer is the outermost machine (see misc/kumo-format.md,
 * "Choosing the node").
 */
public class MachineTemplate
{

    /** The machine's name (JSON: its key in the enclosing machine's {@code machines}); null for a layer. */
    public String name;

    /**
     * Where the machine (or the layer, when it starts) goes when it is entered and its selector
     * chooses nothing: one of its own nodes or machines. Null: its first node, or its first machine
     * if it has no nodes.
     */
    public String defaultOnEntry;

    /** The machine's own nodes, in declaration order (JSON: an object keyed by node name). */
    public List<NodeTemplate> nodes;

    /** The machine's own nested machines, in declaration order (JSON: an object keyed by name). */
    public List<MachineTemplate> machines;

    /** The selector: an ordered decision tree over the machine's own members. */
    public List<BranchTemplate> select;

    /** Connections out of any node inside the machine. */
    public List<ConnectionTemplate> connections;

    /** The scope's definitions, by name (JSON {@code @define}). */
    public Map<String, DefinitionTemplate> define;
    /** The scope's functions, by name (JSON {@code @functions}). */
    public Map<String, FunctionTemplate> functions;
    /** The scope's statement lists (JSON {@code @on}). */
    public OnTemplate on;

    /** Every node of the machine and of the machines inside it. */
    public List<NodeTemplate> allNodes()
    {
        List<NodeTemplate> all = new ArrayList<>();
        collectNodes(all);
        return all;
    }

    private void collectNodes(List<NodeTemplate> all)
    {
        if (nodes != null)
        {
            all.addAll(nodes);
        }
        if (machines != null)
        {
            for (MachineTemplate machine : machines)
            {
                machine.collectNodes(all);
            }
        }
    }

}
