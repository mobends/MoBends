package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.SpringTemplate;

/** See {@link SpringTemplate}: a node variable pulled towards a target like a mass on a spring. */
public class SpringDriver implements IPoseItem
{

    /** The integration step, in ticks: small enough to stay stable for any stiffness an animator would use. */
    private static final float MAX_STEP = 0.25F;

    /** The node variable it writes. */
    private final int variable;
    private final Expression target;
    private final float stiffness;
    private final float friction;
    private final float initial;
    private float value;
    private float velocity;

    public SpringDriver(int variable, Expression target, float stiffness, float friction, float initial)
    {
        this.variable = variable;
        this.target = target;
        this.stiffness = stiffness;
        this.friction = friction;
        this.initial = initial;
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, SpringTemplate template) throws MalformedKumoTemplateException
    {
        if (template.name == null || template.target == null)
        {
            throw new MalformedKumoTemplateException("core:spring needs a 'name' and a 'target'.");
        }
        if (template.stiffness < 0 || template.friction < 0)
        {
            throw new MalformedKumoTemplateException("core:spring's 'stiffness' and 'friction' can't be negative.");
        }
        return new SpringDriver(context.getExpressionScope().getVariables().nodeVariable(template.name), Expression.compile(template.target, context.getExpressionScope(), null),
                template.stiffness, template.friction, template.initial);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        float goal = target.get(context);
        float remaining = context.getDeltaTime();
        while (remaining > 0)
        {
            float dt = Math.min(remaining, MAX_STEP);
            // Semi-implicit Euler: the velocity first, then the value with the new velocity.
            velocity += ((goal - value) * stiffness - velocity * friction) * dt;
            value += velocity * dt;
            remaining -= dt;
        }
        context.getNodeScope().set(variable, value);
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
        value = initial;
        velocity = 0;
        context.getNodeScope().set(variable, initial);
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

}
