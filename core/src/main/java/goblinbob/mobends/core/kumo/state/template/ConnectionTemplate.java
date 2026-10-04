package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

import java.util.List;
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

    /** Statements run when the connection fires (JSON {@code do}), before the scopes it enters start. */
    public List<StatementTemplate> run;

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
