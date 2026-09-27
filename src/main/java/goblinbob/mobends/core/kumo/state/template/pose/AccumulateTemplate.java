package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

/**
 * {@code "driver": "core:accumulate"}: a node-local variable that grows by {@code rate} per tick
 * (the phase of a wiggle whose speed changes over time). Reset to {@code initial} on node entry.
 */
public class AccumulateTemplate extends DriverItemTemplate
{

    public String name;

    /** Growth per tick; an expression (e.g. a decaying ramp). */
    public ExpressionTemplate rate;

    public float initial = 0;

    public float min = Float.NEGATIVE_INFINITY;
    public float max = Float.POSITIVE_INFINITY;

}
