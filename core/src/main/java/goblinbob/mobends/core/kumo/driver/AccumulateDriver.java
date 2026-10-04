package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.AccumulateTemplate;

/** See {@link AccumulateTemplate}: {@code value += rate * deltaTime} every frame, exposed as a node variable. */
public class AccumulateDriver implements IPoseItem
{

    /** The node variable it writes. */
    private final int variable;
    private final Expression rate;
    private final float initial;
    private final float min;
    private final float max;
    private float value;

    public AccumulateDriver(int variable, Expression rate, float initial, float min, float max)
    {
        this.variable = variable;
        this.rate = rate;
        this.initial = initial;
        this.min = min;
        this.max = max;
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, AccumulateTemplate template) throws MalformedKumoTemplateException
    {
        if (template.name == null || template.rate == null)
        {
            throw new MalformedKumoTemplateException("core:accumulate needs a 'name' and a 'rate'.");
        }
        return new AccumulateDriver(context.getExpressionScope().getVariables().nodeVariable(template.name), Expression.compile(template.rate, context.getExpressionScope(), null), template.initial, template.min, template.max);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        value += rate.get(context) * context.getDeltaTime();
        if (value < min) value = min;
        if (value > max) value = max;
        context.getNodeScope().set(variable, value);
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
        value = initial;
        context.getNodeScope().set(variable, initial);
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

}
