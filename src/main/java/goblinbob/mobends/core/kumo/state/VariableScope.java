package goblinbob.mobends.core.kumo.state;

import java.util.HashMap;
import java.util.Map;

/** A small named-number store: layer variables (combo counters) and node-local ramps. */
public class VariableScope
{

    private final Map<String, Double> values = new HashMap<>();

    public boolean has(String name)
    {
        return values.containsKey(name);
    }

    public double get(String name)
    {
        Double value = values.get(name);
        return value == null ? 0 : value;
    }

    public void set(String name, double value)
    {
        values.put(name, value);
    }

    public void putAll(Map<String, Float> initial)
    {
        if (initial != null)
        {
            for (Map.Entry<String, Float> entry : initial.entrySet())
            {
                values.put(entry.getKey(), (double) entry.getValue());
            }
        }
    }

}
