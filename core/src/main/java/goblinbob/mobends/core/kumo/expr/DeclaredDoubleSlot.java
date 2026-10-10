package goblinbob.mobends.core.kumo.expr;

import goblinbob.mobends.core.kumo.api.DoubleArraySlot;
import goblinbob.mobends.core.kumo.api.DoubleSlot;
import goblinbob.mobends.core.kumo.api.EvalContext;
import goblinbob.mobends.core.kumo.state.EntityState;
import goblinbob.mobends.core.kumo.state.StateLayout;

import java.util.Arrays;

/**
 * Doubles an operation or a driver declared: in the entity's state (see {@link StateLayout}), read
 * through the context it evaluates against, as {@link DeclaredSlot} reads floats.
 */
public final class DeclaredDoubleSlot implements DeclaredState, DoubleSlot, DoubleArraySlot
{

    private final int offset;
    private final int size;
    private final double initial;

    public DeclaredDoubleSlot(StateLayout layout, int size, double initial)
    {
        this.offset = layout.doubles(size, initial);
        this.size = size;
        this.initial = initial;
    }

    @Override
    public void reset(EntityState state)
    {
        Arrays.fill(state.doubles, offset, offset + size, initial);
    }

    private static double[] doubles(EvalContext context)
    {
        return ((DeclaredSlot.Access) context).state().doubles;
    }

    @Override
    public double get(EvalContext context)
    {
        return doubles(context)[offset];
    }

    @Override
    public void set(EvalContext context, double value)
    {
        doubles(context)[offset] = value;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public double get(EvalContext context, int index)
    {
        return doubles(context)[offset + index];
    }

    @Override
    public void set(EvalContext context, int index, double value)
    {
        doubles(context)[offset + index] = value;
    }

}
