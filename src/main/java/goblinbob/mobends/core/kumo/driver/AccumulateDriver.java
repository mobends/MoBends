package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.pose.ValueSource;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.AccumulateTemplate;

/** See {@link AccumulateTemplate}: {@code value += rate * deltaTime} every frame, exposed as a node variable. */
public class AccumulateDriver implements IPoseItem
{

    private final String name;
    private final ValueSource rate;
    private final float initial;
    private final float min;
    private final float max;
    private float value;

    public AccumulateDriver(String name, ValueSource rate, float initial, float min, float max)
    {
        this.name = name;
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
        return new AccumulateDriver(template.name, ValueSource.fromTemplate(template.rate, null), template.initial, template.min, template.max);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        value += rate.get(context) * context.getDeltaTime();
        if (value < min) value = min;
        if (value > max) value = max;
        context.getNodeScope().set(name, value);
    }

    @Override
    public boolean isFinished(float elapsedTicks)
    {
        return false;
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
        value = initial;
        context.getNodeScope().set(name, initial);
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

}
