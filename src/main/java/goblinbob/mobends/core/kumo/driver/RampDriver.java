package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.RampTemplate;

/**
 * A node-local 0..1 variable that moves towards 1 while {@code when} holds (or always, without a
 * condition) and back towards 0 otherwise, at {@code speed} per tick. Resets to 0 when the node
 * is entered. This is the "transformTransition" / "bringUpAnimation" idiom of the procedural
 * bits; items after it in the pose stack read it through {@code {"variable": "<name>"}}.
 */
public class RampDriver implements IPoseItem
{

    private final String name;
    private final float upSpeed;
    private final float downSpeed;
    private final ITriggerCondition when;
    private final boolean readBeforeAdvance;
    private float value;

    public RampDriver(String name, float upSpeed, float downSpeed, ITriggerCondition when, boolean readBeforeAdvance)
    {
        this.name = name;
        this.upSpeed = upSpeed;
        this.downSpeed = downSpeed;
        this.when = when;
        this.readBeforeAdvance = readBeforeAdvance;
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, RampTemplate template) throws MalformedKumoTemplateException
    {
        if (template.name == null)
        {
            throw new MalformedKumoTemplateException("core:ramp needs a 'name'.");
        }
        ITriggerCondition when = template.when == null ? null : TriggerConditionRegistry.instance.createFromTemplate(template.when);
        return new RampDriver(template.name, template.speed, template.downSpeed == null ? template.speed : template.downSpeed, when, template.readBeforeAdvance);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        // Most bits advance their ramp at the top of perform(), i.e. before using it; some
        // compute their eased value first and advance afterwards (readBeforeAdvance).
        if (readBeforeAdvance)
        {
            context.getNodeScope().set(name, value);
        }
        boolean up = when == null || when.isConditionMet(context);
        float dt = context.getDeltaTime();
        if (up)
        {
            value = Math.min(value + dt * upSpeed, 1F);
        }
        else if (downSpeed > 0)
        {
            value = Math.max(value - dt * downSpeed, 0F);
        }
        if (!readBeforeAdvance)
        {
            context.getNodeScope().set(name, value);
        }
    }

    @Override
    public boolean isFinished(float elapsedTicks)
    {
        return false;
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
        value = 0;
        context.getNodeScope().set(name, 0);
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

}
