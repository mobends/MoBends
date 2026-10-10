package goblinbob.mobends.core.kumo.api;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;

/** What a {@link Binder} gets: the arguments written out, the entity's class, and its state. */
public interface BindArgs
{

    /** The number of arguments (more than the parameters if the last repeats). */
    int count();

    /** A string or choice argument. */
    String string(int index);

    /** A constant argument (a number written out). */
    float constant(int index);

    /** The class of the entity animated, or null if it is unknown. */
    @Nullable
    Class<?> entityClass();

    /** An error about argument {@code index}, in the operation's own words; throw it. */
    MalformedKumoTemplateException error(int index, String message);

    /**
     * A number this use of the operation keeps between frames, {@code initial} whenever the scope
     * holding it starts (a node, when it is entered). A boolean is kept as 0 or 1.
     */
    FloatSlot slot(String name, float initial);

    /** {@code size} numbers it keeps (one per leg, say), each {@code initial} whenever the scope holding it starts. */
    FloatArraySlot slots(String name, int size, float initial);

    /** A double it keeps between frames (a position in the world, which a float would round); see {@link #slot}. */
    DoubleSlot doubleSlot(String name, double initial);

    /** {@code size} doubles it keeps between frames; see {@link #slots}. */
    DoubleArraySlot doubleSlots(String name, int size, double initial);

}
