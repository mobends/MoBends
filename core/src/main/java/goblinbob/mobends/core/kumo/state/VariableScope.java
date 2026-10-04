package goblinbob.mobends.core.kumo.state;

import java.util.Arrays;

/**
 * The variables of a layer (combo counters) or a node (ramps), by their number in the animator's
 * {@link VariableTable}. A variable exists once written, and stays for the scope's life.
 */
public class VariableScope
{

    private double[] values = new double[0];
    private boolean[] written = new boolean[0];

    public boolean has(int id)
    {
        return id < written.length && written[id];
    }

    /** The value, 0 if never written. */
    public double get(int id)
    {
        return id < values.length ? values[id] : 0;
    }

    public void set(int id, double value)
    {
        if (id >= values.length)
        {
            int size = Math.max(id + 1, values.length * 2);
            values = Arrays.copyOf(values, size);
            written = Arrays.copyOf(written, size);
        }
        values[id] = value;
        written[id] = true;
    }

}
