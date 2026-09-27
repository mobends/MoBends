package goblinbob.mobends.core.kumo.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A small named-number store: layer variables (combo counters) and node-local ramps. Scopes hold a
 * handful of names and are read and written every frame, so the values are kept unboxed and
 * looked up by a linear scan.
 */
public class VariableScope
{

    private final List<String> names = new ArrayList<>();
    private double[] values = new double[4];

    private int indexOf(String name)
    {
        for (int i = 0; i < names.size(); i++)
        {
            if (names.get(i).equals(name))
            {
                return i;
            }
        }
        return -1;
    }

    public boolean has(String name)
    {
        return indexOf(name) >= 0;
    }

    public double get(String name)
    {
        int index = indexOf(name);
        return index < 0 ? 0 : values[index];
    }

    public void set(String name, double value)
    {
        int index = indexOf(name);
        if (index < 0)
        {
            index = names.size();
            names.add(name);
            if (index == values.length)
            {
                values = Arrays.copyOf(values, index * 2);
            }
        }
        values[index] = value;
    }

    public void putAll(Map<String, Float> initial)
    {
        if (initial != null)
        {
            for (Map.Entry<String, Float> entry : initial.entrySet())
            {
                set(entry.getKey(), entry.getValue());
            }
        }
    }

}
