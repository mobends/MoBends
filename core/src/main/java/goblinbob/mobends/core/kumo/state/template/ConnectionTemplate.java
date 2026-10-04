package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

import java.util.Map;

public class ConnectionTemplate
{

    /** Name of the target node. */
    public String target;

    /** The connection's condition (JSON {@code when}). */
    public ExpressionTemplate when;

    /** The duration of the transition in ticks. */
    public float transitionDuration = 0;

    public Easing transitionEasing = Easing.EASE_IN_OUT;

    /** Layer variables assigned when this connection fires (before the target node starts). */
    public Map<String, Float> set;

    public enum Easing
    {
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT,
        /** 1 - e^(-4t): the step response of the bones' exponential smoothing. */
        EXPONENTIAL,
    }

}
