package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.state.template.ValueTemplate;

/**
 * {@code "driver": "core:accumulate"}: a node-local variable that grows by {@code rate} per tick
 * (the phase of a wiggle whose speed changes over time). Reset to {@code initial} on node entry.
 */
public class AccumulateTemplate extends DriverItemTemplate
{

    public String name;

    /** Growth per tick; a number or a value source (e.g. a decaying ramp). */
    public ValueTemplate rate;

    public float initial = 0;

}
