package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The entity's values an animator reads by bare name (its variables, such as {@code limbSwing}, and
 * states, such as {@code ON_GROUND}), looked up once, when the animator is bound to the entity on
 * its first frame ({@link #bind}): a frame never looks a name up by string. A name the entity
 * doesn't have fails the animator then, before it animates.
 */
public final class VariableTable
{

    private final Map<String, Read> reads = new HashMap<>();
    private final Map<String, State> states = new HashMap<>();
    private final List<Read> readList = new ArrayList<>();
    private final List<State> stateList = new ArrayList<>();

    /** The read of the entity's variable {@code name}. */
    public Read read(String name)
    {
        Read read = reads.get(name);
        if (read == null)
        {
            read = new Read(name, readList.size());
            reads.put(name, read);
            readList.add(read);
        }
        return read;
    }

    /** The read of the entity's state {@code name} (e.g. {@code ON_GROUND}). */
    public State state(String name)
    {
        State state = states.get(name);
        if (state == null)
        {
            state = new State(name, stateList.size());
            states.put(name, state);
            stateList.add(state);
        }
        return state;
    }

    /**
     * Looks every name read up on the subject, into the entity's state. Done for the subject of
     * the first frame; the state belongs to it.
     *
     * @throws MalformedKumoTemplateException if the entity has no such variable or state.
     */
    void bind(IKumoSubject subject, EntityState entity) throws MalformedKumoTemplateException
    {
        int[] variables = new int[readList.size()];
        for (Read read : readList)
        {
            int index = variables[read.id] = subject.indexOfVariable(read.name);
            if (index < 0)
            {
                throw new MalformedKumoTemplateException("Unknown variable '" + read.name + "': the entity has none (a name the animator declares has a scope, such as 'layer." + read.name + "').");
            }
        }
        int[] states = new int[stateList.size()];
        for (State state : stateList)
        {
            int index = states[state.id] = subject.indexOfState(state.name);
            if (index < 0)
            {
                throw new MalformedKumoTemplateException("Unknown state '" + state.name + "': the entity has none.");
            }
        }
        entity.variableIndices = variables;
        entity.stateIndices = states;
    }

    /** One of the entity's variables, looked up once for the whole animator. */
    public static final class Read
    {

        public final String name;
        /** Its index among the animator's reads, where the entity's state has the subject's index of it. */
        final int id;

        private Read(String name, int id)
        {
            this.name = name;
            this.id = id;
        }

        public double get(ITriggerConditionContext context)
        {
            return context.getSubject().getVariable(context.getState().variableIndices[id]);
        }

    }

    /** One of the entity's states, looked up once for the whole animator. */
    public static final class State
    {

        public final String name;
        /** Its index among the animator's states, where the entity's state has the subject's index of it. */
        final int id;

        private State(String name, int id)
        {
            this.name = name;
            this.id = id;
        }

        public boolean get(ITriggerConditionContext context)
        {
            return context.getSubject().getState(context.getState().stateIndices[id]);
        }

    }

}
