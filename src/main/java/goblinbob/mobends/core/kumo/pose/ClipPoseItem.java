package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.expr.Expression;

import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

/**
 * A keyframe clip, weighted by an {@link Expression} and composed into the pose in a given space.
 * Where in the clip it is comes from {@code frame}, in the clip's own units (0 to {@code clipLength},
 * see {@code KeyframeAnimation.duration}); a frame outside that range holds the first or last
 * keyframe. {@code duration} (ticks, optional) is how long the item runs: with one the clip is
 * fitted to it by default and is finished once it has passed; without one the clip plays at a unit
 * per tick by default and never finishes.
 */
public class ClipPoseItem implements IPoseItem
{

    private final ClipBinding clip;
    /** Null for the default frame. */
    private final Expression frame;
    private final Expression weight;
    /** Explicit space, or null to use the layer's per-bone default. */
    private final Pose.Space space;
    private final ITriggerCondition when;
    private final float clipLength;
    /** Ticks, or NaN for none. */
    private final float duration;
    private final LayerSpaces layerSpaces;
    private final ItemEffects effects;
    private Pose.Space[] resolvedSpaces;
    private int[] writtenSlots;

    public ClipPoseItem(ClipBinding clip, Expression frame, Expression weight, Pose.Space space, ITriggerCondition when, float clipLength, float duration, LayerSpaces layerSpaces, ItemEffects effects)
    {
        this.clip = clip;
        this.frame = frame;
        this.weight = weight;
        this.space = space;
        this.when = when;
        this.clipLength = clipLength;
        this.duration = duration;
        this.layerSpaces = layerSpaces;
        this.effects = effects;
    }

    /** How far through the clip {@code frame} (or, for the default frame, the elapsed ticks) is, 0 to 1. */
    private float progress(IKumoContext context, float elapsedTicks)
    {
        if (frame != null)
        {
            return frame.get(context) / clipLength;
        }
        return Float.isNaN(duration) ? elapsedTicks / clipLength : elapsedTicks / duration;
    }

    public float keyframeIndexAt(float fraction)
    {
        int last = clip.keyframeCount - 1;
        if (last <= 0 || clipLength <= 0 || Float.isNaN(fraction))
        {
            return 0;
        }
        if (fraction < 0)
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

        // Explicit times: find the interval [times[i], times[i+1]) containing the time.
        float time = fraction * clipLength;
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
        float w = weight.get(context);
        if (w == 0F)
        {
            return;
        }
        float fraction = progress(context, elapsedTicks);
        if (resolvedSpaces == null)
        {
            resolvedSpaces = layerSpaces.resolve(clip.boneSlots(), space);
            writtenSlots = clip.writtenSlots();
        }
        clip.apply(pose, keyframeIndexAt(fraction), w, space, resolvedSpaces);
        if (effects != null)
        {
            effects.apply(pose, writtenSlots, context);
        }
    }

    @Override
    public boolean isFinished(float elapsedTicks)
    {
        return elapsedTicks >= duration;
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
