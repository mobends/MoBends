package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.state.StateRef;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.SpringTemplate;

/** See {@link SpringTemplate}: a state pulled towards a target like a mass on a spring. */
public class SpringDriver implements IPoseItem
{

    /** The integration step, in ticks: small enough to stay stable for any stiffness an animator would use. */
    private static final float MAX_STEP = 0.25F;

    private final StateRef state;
    private final Expression target;
    private final float stiffness;
    private final float friction;
    private float velocity;

    public SpringDriver(StateRef state, Expression target, float stiffness, float friction)
    {
        this.state = state;
        this.target = target;
        this.stiffness = stiffness;
        this.friction = friction;
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, SpringTemplate template) throws MalformedKumoTemplateException
    {
        if (template.inout == null || template.target == null)
        {
            throw new MalformedKumoTemplateException("core:spring needs an 'inout' (the state it steps) and a 'target'.");
        }
        if (template.stiffness < 0 || template.friction < 0)
        {
            throw new MalformedKumoTemplateException("core:spring's 'stiffness' and 'friction' can't be negative.");
        }
        return new SpringDriver(DriverStates.number(context, template.inout, "core:spring"), Expression.compile(template.target, context.getExpressionScope(), null),
                template.stiffness, template.friction);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        float goal = target.get(context);
        float value = (float) state.get(context);
        float remaining = context.getDeltaTime();
        while (remaining > 0)
        {
            float dt = Math.min(remaining, MAX_STEP);
            // Semi-implicit Euler: the velocity first, then the value with the new velocity.
            velocity += ((goal - value) * stiffness - velocity * friction) * dt;
            value += velocity * dt;
            remaining -= dt;
        }
        state.set(value);
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
        velocity = 0;
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

}
