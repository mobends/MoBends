package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.pose.ValueSource;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.OffsetTemplate;

/** See {@link OffsetTemplate}. The offset replaces (OVERRIDE) unless the item's space says otherwise. */
public class OffsetDriver implements IPoseItem
{

    private final int slot;
    private final ValueSource x;
    private final ValueSource y;
    private final ValueSource z;
    private final Pose.Space space;

    public OffsetDriver(int slot, ValueSource x, ValueSource y, ValueSource z, Pose.Space space)
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
                ValueSource.fromTemplate(template.x, ValueSource.ZERO),
                ValueSource.fromTemplate(template.y, ValueSource.ZERO),
                ValueSource.fromTemplate(template.z, ValueSource.ZERO),
                template.space == null ? Pose.Space.OVERRIDE : template.space);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        pose.composeOffset(slot, x.get(context), y.get(context), z.get(context), space);
    }

    @Override
    public boolean isFinished(float elapsedTicks)
    {
        return false;
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
