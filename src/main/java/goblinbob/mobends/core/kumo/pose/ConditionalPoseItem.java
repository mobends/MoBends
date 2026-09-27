package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

/**
 * Applies the wrapped item only while its {@code when} condition holds (the in-bit "if" idiom),
 * so every item kind supports the condition the same way.
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
    public boolean isFinished(float elapsedTicks)
    {
        return item.isFinished(elapsedTicks);
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
