package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

/**
 * Applies the wrapped item only while its {@code when} condition holds, so every item kind
 * supports the condition the same way (except ramps, whose {@code when} is their own switch).
 */
public class ConditionalPoseItem implements IPoseItem
{

    private final IPoseItem item;
    private final ITriggerCondition when;

    public ConditionalPoseItem(IPoseItem item, ITriggerCondition when)
    {
        this.item = item;
        this.when = when;
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        if (when.isConditionMet(context))
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
        when.onNodeStarted(context);
        item.onNodeStarted(context);
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
        item.advance(context, deltaTime);
    }

}
