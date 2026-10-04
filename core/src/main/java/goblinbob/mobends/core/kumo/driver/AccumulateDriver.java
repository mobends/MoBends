package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.state.StateRef;
import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.AccumulateTemplate;

/** See {@link AccumulateTemplate}: {@code state += rate * deltaTime} every frame, clamped. */
public class AccumulateDriver implements IPoseItem
{

    private final StateRef state;
    private final Expression rate;
    private final float min;
    private final float max;

    public AccumulateDriver(StateRef state, Expression rate, float min, float max)
    {
        this.state = state;
        this.rate = rate;
        this.min = min;
        this.max = max;
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, AccumulateTemplate template) throws MalformedKumoTemplateException
    {
        if (template.inout == null || template.rate == null)
        {
            throw new MalformedKumoTemplateException("core:accumulate needs an 'inout' (the state it steps) and a 'rate'.");
        }
        return new AccumulateDriver(DriverStates.number(context, template.inout, "core:accumulate"), Expression.compile(template.rate, context.getExpressionScope(), null), template.min, template.max);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        float value = (float) state.get(context) + rate.get(context) * context.getDeltaTime();
        if (value < min) value = min;
        if (value > max) value = max;
        state.set(value);
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
