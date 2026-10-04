package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The names an animator reads and writes, numbered when it is instanced, so a frame never looks a
 * name up by string. Writers number the node and layer variables they write; every read of a name
 * shares one {@link Read}, which is resolved once every writer is known ({@link #link}) and against
 * the subject's variables ({@link #bind}). A name that nothing writes and the subject doesn't have
 * fails the animator before it animates.
 */
public final class VariableTable
{

    private final Map<String, Integer> nodeIds = new HashMap<>();
    private final Map<String, Integer> layerIds = new HashMap<>();
    private final Map<String, Read> reads = new HashMap<>();
    private final Map<String, State> states = new HashMap<>();
    private final List<Read> readList = new ArrayList<>();
    private final List<State> stateList = new ArrayList<>();
    @Nullable
    private IKumoSubject boundTo;

    /** The number of a node variable, for a writer of it (ramps, accumulators, springs, ...). */
    public int nodeVariable(String name)
    {
        return number(nodeIds, name);
    }

    /** The number of a layer variable, for a writer of it (a layer's variables, {@code set}, ...). */
    public int layerVariable(String name)
    {
        return number(layerIds, name);
    }

    private static int number(Map<String, Integer> ids, String name)
    {
        Integer id = ids.get(name);
        if (id == null)
        {
            id = ids.size();
            ids.put(name, id);
        }
        return id;
    }

    /** Values assigned by name, numbered as layer variables. */
    public Assignments layerAssignments(@Nullable Map<String, Float> values)
    {
        if (values == null || values.isEmpty())
        {
            return Assignments.NONE;
        }
        int[] ids = new int[values.size()];
        float[] assigned = new float[values.size()];
        int i = 0;
        for (Map.Entry<String, Float> entry : values.entrySet())
        {
            ids[i] = layerVariable(entry.getKey());
            assigned[i] = entry.getValue();
            i++;
        }
        return new Assignments(ids, assigned);
    }

    /** The read of {@code name}: the node's variable if written, else the layer's, else the subject's. */
    public Read read(String name)
    {
        Read read = reads.get(name);
        if (read == null)
        {
            read = new Read(name);
            reads.put(name, read);
            readList.add(read);
        }
        return read;
    }

    /** The read of the subject's state {@code name} (e.g. {@code ON_GROUND}). */
    public State state(String name)
    {
        State state = states.get(name);
        if (state == null)
        {
            state = new State(name);
            states.put(name, state);
            stateList.add(state);
        }
        return state;
    }

    /**
     * Resolves every read against the node and layer variables, once every writer has numbered its
     * own (the animator does, when its layers are instanced).
     */
    public void link()
    {
        for (Read read : readList)
        {
            Integer node = nodeIds.get(read.name);
            Integer layer = layerIds.get(read.name);
            read.nodeId = node == null ? -1 : node;
            read.layerId = layer == null ? -1 : layer;
        }
    }

    /**
     * Resolves every read against the subject's variables and states. Done for the subject of the
     * first frame; the animator belongs to it.
     *
     * @throws MalformedKumoTemplateException if a name is no variable of the animator or the subject.
     */
    void bind(IKumoSubject subject) throws MalformedKumoTemplateException
    {
        if (boundTo == subject)
        {
            return;
        }
        for (Read read : readList)
        {
            read.subjectIndex = subject.indexOfVariable(read.name);
            if (read.subjectIndex < 0 && read.nodeId < 0 && read.layerId < 0)
            {
                throw new MalformedKumoTemplateException("Unknown variable '" + read.name + "': no node or layer variable, and the entity has none.");
            }
        }
        for (State state : stateList)
        {
            state.index = subject.indexOfState(state.name);
            if (state.index < 0)
            {
                throw new MalformedKumoTemplateException("Unknown state '" + state.name + "': the entity has none.");
            }
        }
        boundTo = subject;
    }

    /** One name as read by expressions and drivers, resolved once for the whole animator. */
    public static final class Read
    {

        public final String name;
        int nodeId = -1;
        int layerId = -1;
        int subjectIndex = -1;

        private Read(String name)
        {
            this.name = name;
        }

        /**
         * The value: the node's variable once written, else the layer's once written, else the
         * subject's.
         */
        public double get(VariableScope node, VariableScope layer, IKumoSubject subject)
        {
            if (nodeId >= 0 && node.has(nodeId)) return node.get(nodeId);
            if (layerId >= 0 && layer.has(layerId)) return layer.get(layerId);
            if (subjectIndex >= 0) return subject.getVariable(subjectIndex);
            throw new IllegalStateException("The variable '" + name + "' is read before anything writes it.");
        }

    }

    /** One of the subject's states, resolved once for the whole animator. */
    public static final class State
    {

        public final String name;
        int index = -1;

        private State(String name)
        {
            this.name = name;
        }

        public boolean get(IKumoSubject subject)
        {
            return subject.getState(index);
        }

    }

    /** Layer variables to assign, numbered (a node's, a branch's or a connection's {@code set}). */
    public static final class Assignments
    {

        public static final Assignments NONE = new Assignments(new int[0], new float[0]);

        private final int[] ids;
        private final float[] values;

        private Assignments(int[] ids, float[] values)
        {
            this.ids = ids;
            this.values = values;
        }

        public void applyTo(VariableScope scope)
        {
            for (int i = 0; i < ids.length; i++)
            {
                scope.set(ids[i], values[i]);
            }
        }

    }

}
