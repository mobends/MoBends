package goblinbob.mobends.core.kumo.expr;

import goblinbob.mobends.core.kumo.api.EvalContext;
import goblinbob.mobends.core.kumo.api.FloatArraySlot;
import goblinbob.mobends.core.kumo.api.FloatSlot;

import java.util.Arrays;

/**
 * State an operation or a driver declared, kept here for its one use: an animator is compiled
 * for each entity it animates (until program and state split, when it moves into the entity's
 * state array and the handle reads it through the context).
 */
public final class DeclaredSlot implements FloatSlot, FloatArraySlot
{

    private final float[] values;
    private final float initial;

    public DeclaredSlot(int size, float initial)
    {
        this.values = new float[size];
        this.initial = initial;
        Arrays.fill(values, initial);
    }

    /** Back to the initial values: the scope holding the use started. */
    public void reset()
    {
        Arrays.fill(values, initial);
    }

    @Override
    public float get(EvalContext context)
    {
        return values[0];
    }

    @Override
    public void set(EvalContext context, float value)
    {
        values[0] = value;
    }

    @Override
    public int size()
    {
        return values.length;
    }

    @Override
    public float get(EvalContext context, int index)
    {
        return values[index];
    }

    @Override
    public void set(EvalContext context, int index, float value)
    {
        values[index] = value;
    }

}
