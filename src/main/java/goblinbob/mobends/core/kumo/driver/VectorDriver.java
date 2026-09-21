package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.pose.*;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.VectorTemplate;

/**
 * Writes a vector target (e.g. the global offset) from three value sources:
 * {@code {"driver": "core:vector", "bone": "root", "y": {"variable": "deep", "scale": 14}}}.
 */
public class VectorDriver implements IPoseItem
{

    private final int slot;
    private final ValueSource x, y, z;
    private final Pose.Space space;
    private final ITriggerCondition when;
    private final ItemEffects effects;
    private final int[] written;

    public VectorDriver(int slot, ValueSource x, ValueSource y, ValueSource z, Pose.Space space, ITriggerCondition when, ItemEffects effects)
    {
        this.slot = slot;
        this.x = x;
        this.y = y;
        this.z = z;
        this.space = space;
        this.when = when;
        this.effects = effects;
        this.written = new int[] { slot };
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, VectorTemplate template) throws MalformedKumoTemplateException
    {
        if (template.bone == null)
        {
            throw new MalformedKumoTemplateException("core:vector needs a 'bone'.");
        }
        ITriggerCondition when = template.when == null ? null : TriggerConditionRegistry.instance.createFromTemplate(template.when);
        ItemEffects effects = new ItemEffects(skeleton, template.damping, template.vectorModes, template.snap);
        return new VectorDriver(skeleton.indexOf(template.bone),
                ValueSource.fromTemplate(template.x, ValueSource.ZERO),
                ValueSource.fromTemplate(template.y, ValueSource.ZERO),
                ValueSource.fromTemplate(template.z, ValueSource.ZERO),
                template.space == null ? Pose.Space.OVERRIDE : template.space,
                when, effects.isEmpty() ? null : effects);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        if (when != null && !when.isConditionMet(context))
        {
            return;
        }
        pose.composeVector(slot, x.get(context), y.get(context), z.get(context), space);
        if (effects != null)
        {
            effects.apply(pose, written, context);
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
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

}
