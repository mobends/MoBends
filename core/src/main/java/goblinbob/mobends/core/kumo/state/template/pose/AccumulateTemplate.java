package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

/**
 * {@code {"core:accumulate": {...}}}: a state (named by {@code inout}) that grows by {@code rate}
 * per tick, clamped to {@code min}..{@code max} (the phase of a wiggle whose speed changes over
 * time; with 0..1 and a rate whose sign follows a condition, a ramp up and down). It starts where
 * its state does.
 */
public class AccumulateTemplate extends DriverItemTemplate
{

    /** The state it steps (a number state, e.g. {@code node.phase}). */
    public String inout;

    /** Growth per tick; an expression (e.g. a decaying ramp). */
    public ExpressionTemplate rate;

    public float min = Float.NEGATIVE_INFINITY;
    public float max = Float.POSITIVE_INFINITY;

}
