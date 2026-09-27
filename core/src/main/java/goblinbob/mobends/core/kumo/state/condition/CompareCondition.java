package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

/**
 * Compares two expressions:
 * {@code {"type": "core:compare", "left": "ticksAfterTouchdown", "op": "<", "right": 1}}.
 *
 * @author Iwo Plaza
 */
public class CompareCondition implements ITriggerCondition
{

    private final Expression left;
    private final Op op;
    private final Expression right;

    public CompareCondition(Template template, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        if (template.left == null || template.right == null)
        {
            throw new MalformedKumoTemplateException("core:compare needs a 'left' and a 'right' expression.");
        }
        this.left = Expression.compile(template.left.json, scope);
        this.op = Op.parse(template.op);
        this.right = Expression.compile(template.right.json, scope);
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context)
    {
        float a = left.get(context);
        float b = right.get(context);
        switch (op)
        {
            case LESS: return a < b;
            case LESS_OR_EQUAL: return a <= b;
            case GREATER: return a > b;
            case GREATER_OR_EQUAL: return a >= b;
            case EQUAL: return a == b;
            case NOT_EQUAL: return a != b;
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

        public ExpressionTemplate left;
        public String op;
        public ExpressionTemplate right;

    }

}
