package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

/**
 * Compares a subject variable with a constant:
 * {@code {"type": "core:compare", "variable": "ticksAfterTouchdown", "op": "<", "value": 1}}.
 *
 * @author Iwo Plaza
 */
public class CompareCondition implements ITriggerCondition
{

    private final String variable;
    private final Op op;
    private final double value;

    public CompareCondition(Template template) throws MalformedKumoTemplateException
    {
        if (template.variable == null)
        {
            throw new MalformedKumoTemplateException("core:compare needs a 'variable'.");
        }
        this.variable = template.variable;
        this.op = Op.parse(template.op);
        this.value = template.value;
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context) throws MalformedKumoTemplateException
    {
        IKumoSubject subject = context.getSubject();
        if (!subject.hasVariable(variable))
        {
            throw new MalformedKumoTemplateException(String.format("Unknown variable '%s' for this subject.", variable));
        }
        double actual = subject.getVariable(variable);
        switch (op)
        {
            case LESS: return actual < value;
            case LESS_OR_EQUAL: return actual <= value;
            case GREATER: return actual > value;
            case GREATER_OR_EQUAL: return actual >= value;
            case EQUAL: return actual == value;
            case NOT_EQUAL: return actual != value;
            default: return false;
        }
    }

    public enum Op
    {
        LESS("<"), LESS_OR_EQUAL("<="), GREATER(">"), GREATER_OR_EQUAL(">="), EQUAL("=="), NOT_EQUAL("!=");

        private final String symbol;

        Op(String symbol)
        {
            this.symbol = symbol;
        }

        static Op parse(String text) throws MalformedKumoTemplateException
        {
            if (text == null)
            {
                throw new MalformedKumoTemplateException("core:compare needs an 'op' (<, <=, >, >=, ==, !=).");
            }
            for (Op op : values())
            {
                if (op.symbol.equals(text) || op.name().equalsIgnoreCase(text))
                {
                    return op;
                }
            }
            throw new MalformedKumoTemplateException(String.format("Unknown comparison operator '%s'.", text));
        }
    }

    public static class Template extends TriggerConditionTemplate
    {

        public String variable;
        public String op;
        public double value;

    }

}
