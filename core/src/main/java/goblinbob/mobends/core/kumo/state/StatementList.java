package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.StatementTemplate;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Statements run in order: {@code {"@when": <condition>, "set": ["layer.combo", <value>]}} sets a
 * state, while its condition holds. Expressions never change state; only statements (and drivers)
 * do.
 */
public final class StatementList
{

    public static final StatementList EMPTY = new StatementList(new Statement[0]);

    private final Statement[] statements;

    private StatementList(Statement[] statements)
    {
        this.statements = statements;
    }

    /** Compiles {@code templates} against {@code scope} (null or empty: {@link #EMPTY}). */
    public static StatementList compile(@Nullable List<StatementTemplate> templates, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        if (templates == null || templates.isEmpty())
        {
            return EMPTY;
        }
        Statement[] statements = new Statement[templates.size()];
        for (int i = 0; i < statements.length; i++)
        {
            StatementTemplate template = templates.get(i);
            StateRef target = scope.resolveState(template.target, "A set statement");
            Expression value = Expression.compile(template.value, scope, target.type);
            Expression when = template.when == null ? null : Expression.compile(template.when, scope, Expression.Type.BOOLEAN);
            statements[i] = new Statement(target, value, when);
        }
        return new StatementList(statements);
    }

    public boolean isEmpty()
    {
        return statements.length == 0;
    }

    public void run(ITriggerConditionContext context)
    {
        for (Statement statement : statements)
        {
            statement.run(context);
        }
    }

    /** Starts the memory of what the statements' expressions remember over. */
    public void restart(ITriggerConditionContext context)
    {
        for (Statement statement : statements)
        {
            if (statement.when != null && statement.when.isStateful()) statement.when.restart(context);
            if (statement.value.isStateful()) statement.value.restart(context);
        }
    }

    private static final class Statement
    {
        private final StateRef target;
        private final Expression value;
        @Nullable
        private final Expression when;

        Statement(StateRef target, Expression value, @Nullable Expression when)
        {
            this.target = target;
            this.value = value;
            this.when = when;
        }

        void run(ITriggerConditionContext context)
        {
            if (when == null || when.test(context))
            {
                target.set(value.getType() == Expression.Type.BOOLEAN ? (value.test(context) ? 1 : 0) : value.get(context), context);
            }
        }
    }

}
