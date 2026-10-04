package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;

/** A state an animator declares, as a statement or a driver writes it (see {@link DefinitionScope#state}). */
public final class StateRef
{

    private final DefinitionScope scope;
    private final int index;
    public final Expression.Type type;
    /** Its scoped name, for messages. */
    public final String name;

    StateRef(DefinitionScope scope, int index, Expression.Type type, String name)
    {
        this.scope = scope;
        this.index = index;
        this.type = type;
        this.name = name;
    }

    public double get(ITriggerConditionContext context)
    {
        return scope.value(index, context);
    }

    public void set(double value)
    {
        scope.set(index, value);
    }

}
