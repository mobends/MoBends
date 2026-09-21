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
    /** Explicit space, or null to use the layer's per-bone default. */
    private final Pose.Space space;
    private final ITriggerCondition when;
    private final float duration;
    private final boolean loop;
    private final LayerSpaces layerSpaces;
    private final ItemEffects effects;
    private Pose.Space[] resolvedSpaces;
    private int[] writtenSlots;

    public ClipPoseItem(ClipBinding clip, TimeSource time, ValueSource weight, Pose.Space space, ITriggerCondition when, float duration, boolean loop, LayerSpaces layerSpaces, ItemEffects effects)
    {
        this.clip = clip;
        this.time = time;
        this.weight = weight;
        this.space = space;
        this.when = when;
        this.duration = duration;
        this.loop = loop;
        this.layerSpaces = layerSpaces;
        this.effects = effects;
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

        float[] times = clip.animation.times;
        if (times == null || times.length != clip.keyframeCount)
        {
            return fraction * last;
        }

        // Explicit times: find the interval [times[i], times[i+1]) containing the wrapped time.
        float time = fraction * duration;
        int i = lastInterval;
        if (i < 0 || i >= last || time < times[i] || time >= times[i + 1])
        {
            i = 0;
            int lo = 0, hi = last;
            while (lo < hi)
            {
                int mid = (lo + hi + 1) >>> 1;
                if (times[mid] <= time) lo = mid; else hi = mid - 1;
            }
            i = Math.min(lo, last - 1);
        }
        lastInterval = i;
        float span = times[i + 1] - times[i];
        float within = span <= 0 ? 0 : (time - times[i]) / span;
        if (within < 0) within = 0;
        if (within > 1) within = 1;
        return i + within;
    }

    private int lastInterval = -1;

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
        if (resolvedSpaces == null)
        {
            resolvedSpaces = layerSpaces.resolve(clip.boneSlots(), space);
            writtenSlots = clip.writtenSlots();
        }
        clip.apply(pose, keyframeIndexAt(t), w, space, resolvedSpaces);
        if (effects != null)
        {
            effects.apply(pose, writtenSlots);
        }
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
