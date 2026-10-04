package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.VariableScope;
import goblinbob.mobends.core.kumo.state.VariableTable;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.SetTemplate;

/**
 * Assigns a variable every frame the item is evaluated (see {@link SetTemplate}). The item's
 * {@code when} condition, handled by the node, makes it conditional.
 */
public class SetDriver implements IPoseItem
{

    /** The number of the variable it writes, in the node's or the layer's scope. */
    private final int variable;
    private final Expression value;
    private final boolean nodeScope;

    public SetDriver(int variable, Expression value, boolean nodeScope)
    {
        this.variable = variable;
        this.value = value;
        this.nodeScope = nodeScope;
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, SetTemplate template) throws MalformedKumoTemplateException
    {
        if (template.variable == null || template.value == null)
        {
            throw new MalformedKumoTemplateException("core:set needs a 'variable' and a 'value'.");
        }
        boolean nodeScope;
        if (template.scope == null || "layer".equals(template.scope))
        {
            nodeScope = false;
        }
        else if ("node".equals(template.scope))
        {
            nodeScope = true;
        }
        else
        {
            throw new MalformedKumoTemplateException("core:set has an unknown 'scope' '" + template.scope + "' (expected \"layer\" or \"node\").");
        }
        VariableTable variables = context.getExpressionScope().getVariables();
        int variable = nodeScope ? variables.nodeVariable(template.variable) : variables.layerVariable(template.variable);
        return new SetDriver(variable, Expression.compile(template.value, context.getExpressionScope(), null), nodeScope);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        VariableScope scope = nodeScope ? context.getNodeScope() : context.getLayerScope();
        scope.set(variable, value.get(context));
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

}
