package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

/**
 * A keyframe clip played back from a {@link TimeSource}, weighted by a {@link ValueSource}, and
 * composed into the pose in a given space. Time is measured in the clip's own duration units
 * (see {@code KeyframeAnimation.duration}); the clip loops when it says so.
 */
public class ClipPoseItem implements IPoseItem
{

    private final ClipBinding clip;
    private final TimeSource time;
    private final ValueSource weight;
    private final Pose.Space space;
    private final ITriggerCondition when;
    private final float duration;
    private final boolean loop;

    public ClipPoseItem(ClipBinding clip, TimeSource time, ValueSource weight, Pose.Space space, ITriggerCondition when, float duration, boolean loop)
    {
        this.clip = clip;
        this.time = time;
        this.weight = weight;
        this.space = space;
        this.when = when;
        this.duration = duration;
        this.loop = loop;
    }

    public float keyframeIndexAt(float t)
    {
        int last = clip.keyframeCount - 1;
        if (last <= 0 || duration <= 0)
        {
            return 0;
        }
        float fraction = t / duration;
        if (loop)
        {
            fraction = fraction - (float) Math.floor(fraction);
        }
        else if (fraction < 0)
        {
            fraction = 0;
        }
        else if (fraction > 1)
        {
            fraction = 1;
        }
        return fraction * last;
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        if (when != null && !when.isConditionMet(context))
        {
            return;
        }
        float w = weight.get(context.getSubject());
        if (w == 0F)
        {
            return;
        }
        float t = time.get(context.getSubject(), elapsedTicks);
        clip.apply(pose, keyframeIndexAt(t), w, space);
    }

    @Override
    public boolean isFinished(float elapsedTicks)
    {
        return !loop && time.isElapsed() && duration > 0 && time.get(null, elapsedTicks) >= duration;
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
