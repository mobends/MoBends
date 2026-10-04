package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

/**
 * Applies the wrapped item only while its {@code when} condition holds, so every item kind
 * supports the condition the same way. The
 * node holding the item starts the condition's memory over (see {@code PoseNode}).
 */
public class ConditionalPoseItem implements IPoseItem
{

    private final IPoseItem item;
    private final Expression when;

    public ConditionalPoseItem(IPoseItem item, Expression when)
    {
        this.item = item;
        this.when = when;
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        if (when.test(context))
        {
            item.apply(pose, context, elapsedTicks);
        }
    }

    @Override
    public float getDuration()
    {
        return item.getDuration();
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
        item.onNodeStarted(context);
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
        item.advance(context, deltaTime);
    }

}
