package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

/**
 * {@code {"core:spring": {...}}}: a state (named by {@code inout}) that follows {@code target}
 * like a mass on a spring, so it lags, overshoots and settles: follow-through of limbs that hang
 * (arms that swing after the body turns, a head that nods after a landing). Its velocity is the
 * spring's own, at rest when the node is entered; the value starts where its state does.
 */
public class SpringTemplate extends DriverItemTemplate
{

    /** The state it steps (a number state, e.g. {@code node.armLag}). */
    public String inout;

    /** What the value is pulled towards; an expression. */
    public ExpressionTemplate target;

    /** Pull per unit of distance, per tick². Higher is faster. */
    public float stiffness = 0.2F;

    /**
     * Loss of velocity per tick. 2·√stiffness settles without overshooting; less swings. (Not
     * {@code damping}, which every item has for the bones it writes.)
     */
    public float friction = 0.4F;

}
