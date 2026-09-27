package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.OffsetTemplate;

/** See {@link OffsetTemplate}. The offset replaces (OVERRIDE) unless the item's space says otherwise. */
public class OffsetDriver implements IPoseItem
{

    private final int slot;
    private final Expression x;
    private final Expression y;
    private final Expression z;
    private final Pose.Space space;

    public OffsetDriver(int slot, Expression x, Expression y, Expression z, Pose.Space space)
    {
        this.slot = slot;
        this.x = x;
        this.y = y;
        this.z = z;
        this.space = space;
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, OffsetTemplate template) throws MalformedKumoTemplateException
    {
        if (template.bone == null)
        {
            throw new MalformedKumoTemplateException("core:offset needs a 'bone'.");
        }
        return new OffsetDriver(skeleton.indexOf(template.bone),
                Expression.compile(template.x, context.getExpressionScope(), Expression.ZERO),
                Expression.compile(template.y, context.getExpressionScope(), Expression.ZERO),
                Expression.compile(template.z, context.getExpressionScope(), Expression.ZERO),
                template.space == null ? Pose.Space.OVERRIDE : template.space);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        pose.composeOffset(slot, x.get(context), y.get(context), z.get(context), space);
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
