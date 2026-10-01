package goblinbob.mobends.core.kumo.state;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** A node or a machine of a layer: where a selector branch or a connection leads. */
public final class MachineMember
{

    public final String name;
    /** The node, or null for a machine. */
    @Nullable
    public final INodeState node;
    /** The machine, or null for a node. */
    @Nullable
    public final MachineState machine;
    /** The machine it belongs to. */
    public final MachineState parent;
    /** A node's own connections (a machine keeps its own in the {@link MachineState}). */
    final List<ConnectionState> connections = new ArrayList<>();

    MachineMember(String name, @Nullable INodeState node, @Nullable MachineState machine, MachineState parent)
    {
        this.name = name;
        this.node = node;
        this.machine = machine;
        this.parent = parent;
    }

    /** Whether this is {@code member} or a machine that has it inside. */
    public boolean contains(MachineMember member)
    {
        for (MachineMember m = member; m != null; m = m.parent.member)
        {
            if (m == this)
            {
                return true;
            }
        }
        return false;
    }

}
