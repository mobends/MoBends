package goblinbob.mobends.core.kumo.state;

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
    @Nullable
    private IKumoSubject boundTo;

    /** The read of the entity's variable {@code name}. */
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

    /** The read of the entity's state {@code name} (e.g. {@code ON_GROUND}). */
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
     * Looks every name read up on the subject. Done for the subject of the first frame; the
     * animator belongs to it.
     *
     * @throws MalformedKumoTemplateException if the entity has no such variable or state.
     */
    void bind(IKumoSubject subject) throws MalformedKumoTemplateException
    {
        if (boundTo == subject)
        {
            return;
        }
        for (Read read : readList)
        {
            read.index = subject.indexOfVariable(read.name);
            if (read.index < 0)
            {
                throw new MalformedKumoTemplateException("Unknown variable '" + read.name + "': the entity has none (a name the animator declares has a scope, such as 'layer." + read.name + "').");
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

    /** One of the entity's variables, looked up once for the whole animator. */
    public static final class Read
    {

        public final String name;
        int index = -1;

        private Read(String name)
        {
            this.name = name;
        }

        public double get(IKumoSubject subject)
        {
            return subject.getVariable(index);
        }

    }

    /** One of the entity's states, looked up once for the whole animator. */
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

}
