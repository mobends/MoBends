package goblinbob.mobends.core.kumo.expr;

import goblinbob.mobends.core.kumo.api.EvalContext;
import goblinbob.mobends.core.kumo.api.FloatArraySlot;
import goblinbob.mobends.core.kumo.api.FloatSlot;
import goblinbob.mobends.core.kumo.state.EntityState;
import goblinbob.mobends.core.kumo.state.StateLayout;

import java.util.Arrays;

/**
 * State an operation or a driver declared: floats in the entity's state (see {@link StateLayout}),
 * read through the context it evaluates against.
 */
public final class DeclaredSlot implements FloatSlot, FloatArraySlot
{

    /** What an operation or a driver evaluates against, inside the engine: it reaches the entity's state. */
    public interface Access
    {
        EntityState state();
    }

    private final int offset;
    private final int size;
    private final float initial;

    public DeclaredSlot(StateLayout layout, int size, float initial)
    {
        this.offset = layout.floats(size, initial);
        this.size = size;
        this.initial = initial;
    }

    /** Back to the initial values: the scope holding the use started. */
    public void reset(EntityState state)
    {
        Arrays.fill(state.floats, offset, offset + size, initial);
    }

    private static float[] floats(EvalContext context)
    {
        return ((Access) context).state().floats;
    }

    @Override
    public float get(EvalContext context)
    {
        return floats(context)[offset];
    }

    @Override
    public void set(EvalContext context, float value)
    {
        floats(context)[offset] = value;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public float get(EvalContext context, int index)
    {
        return floats(context)[offset + index];
    }

    @Override
    public void set(EvalContext context, int index, float value)
    {
        floats(context)[offset + index] = value;
    }

}
