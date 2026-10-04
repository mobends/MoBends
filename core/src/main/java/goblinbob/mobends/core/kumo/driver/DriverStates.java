package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.StateRef;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

/** The states drivers write: named in their fields ({@code inout}, {@code out}), declared in the animator's scopes. */
public final class DriverStates
{

    private DriverStates()
    {
    }

    /** The number state {@code name} (a scoped name), for the driver {@code driver} to write. */
    public static StateRef number(IKumoInstancingContext context, String name, String driver) throws MalformedKumoTemplateException
    {
        StateRef state = context.getExpressionScope().resolveState(name, driver);
        if (state.type != Expression.Type.NUMBER)
        {
            throw new MalformedKumoTemplateException(String.format("%s writes '%s', which is a boolean: it writes numbers.", driver, name));
        }
        return state;
    }

}
