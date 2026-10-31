package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.bind.IVectorSink;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.SmoothingTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Smoothing (in ticks), vector modes and snapping attached to one pose item, applied to the bones the item wrote.
 * An item that moves parts ({@code core:translate} on a part) smooths and snaps their position offsets;
 * any other smooths and snaps rotations, and the entity-level vectors.
 */
public class ItemEffects
{

    /** Whether the effects go to the parts' offsets rather than their rotations. */
    private final boolean offsets;
    private final int[] smoothingSlots;
    /** Per entry of {@link #smoothingSlots} / {@link #modeSlots} / {@link #dynamicSlots}: an entity-level vector bone. */
    private final boolean[] smoothingVector;
    private final float[][] smoothingValues;
    private final float[] defaultSmoothing;
    private final int[] modeSlots;
    private final boolean[] modeVector;
    private final IVectorSink.Mode[] modes;
    private final boolean snap;
    private final int[] dynamicSlots;
    private final boolean[] dynamicVector;
    private final Expression[] dynamicValues;

    /** @param scope Where the smoothing's expressions are compiled. */
    public ItemEffects(ExpressionScope scope, Skeleton skeleton, SmoothingTemplate smoothing, Map<String, IVectorSink.Mode> vectorModes, boolean snap) throws MalformedKumoTemplateException
    {
        this(scope, skeleton, smoothing, vectorModes, snap, false);
    }

    /**
     * @param scope   Where the smoothing's expressions are compiled.
     * @param offsets Whether the item moves parts: its effects go to their offsets (the bones it
     *                writes and the parts it names; the vector bones it names are smoothed as ever).
     */
    public ItemEffects(ExpressionScope scope, Skeleton skeleton, SmoothingTemplate smoothing, Map<String, IVectorSink.Mode> vectorModes, boolean snap, boolean offsets) throws MalformedKumoTemplateException
    {
        this.snap = snap;
        this.offsets = offsets;
        List<String> names = new ArrayList<>();
        List<float[]> values = new ArrayList<>();
        float[] fallback = null;
        if (smoothing != null)
        {
            for (Map.Entry<String, float[]> entry : smoothing.entries.entrySet())
            {
                if (SmoothingTemplate.DEFAULT.equals(entry.getKey()))
                {
                    fallback = entry.getValue();
                }
                else
                {
                    names.add(entry.getKey());
                    values.add(entry.getValue());
                }
            }
        }
        smoothingSlots = new int[names.size()];
        smoothingVector = new boolean[names.size()];
        smoothingValues = new float[names.size()][];
        for (int i = 0; i < smoothingSlots.length; i++)
        {
            smoothingSlots[i] = skeleton.indexOf(names.get(i));
            smoothingVector[i] = Skeleton.isVectorBone(names.get(i));
            smoothingValues[i] = values.get(i);
        }
        defaultSmoothing = fallback;

        List<String> dynNames = new ArrayList<>();
        List<Expression> dynValues = new ArrayList<>();
        if (smoothing != null)
        {
            for (Map.Entry<String, ExpressionTemplate> entry : smoothing.dynamic.entrySet())
            {
                dynNames.add(entry.getKey());
                dynValues.add(Expression.compile(entry.getValue(), scope, Expression.ONE));
            }
        }
        dynamicSlots = new int[dynNames.size()];
        dynamicVector = new boolean[dynNames.size()];
        dynamicValues = new Expression[dynNames.size()];
        for (int k = 0; k < dynamicSlots.length; k++)
        {
            dynamicSlots[k] = skeleton.indexOf(dynNames.get(k));
            dynamicVector[k] = Skeleton.isVectorBone(dynNames.get(k));
            dynamicValues[k] = dynValues.get(k);
        }

        modeSlots = new int[vectorModes == null ? 0 : vectorModes.size()];
        modeVector = new boolean[modeSlots.length];
        modes = new IVectorSink.Mode[modeSlots.length];
        int i = 0;
        if (vectorModes != null)
        {
            for (Map.Entry<String, IVectorSink.Mode> entry : vectorModes.entrySet())
            {
                modeSlots[i] = skeleton.indexOf(entry.getKey());
                modeVector[i] = Skeleton.isVectorBone(entry.getKey());
                modes[i] = entry.getValue();
                i++;
            }
        }
    }

    public boolean isEmpty()
    {
        return smoothingSlots.length == 0 && defaultSmoothing == null && modeSlots.length == 0 && !snap && dynamicSlots.length == 0;
    }

    /** @param writtenSlots the slots the item wrote this frame (the default smoothing applies to those). */
    public void apply(Pose pose, int[] writtenSlots, ITriggerConditionContext context)
    {
        for (int i = 0; i < dynamicSlots.length; i++)
        {
            BoneTarget target = pose.get(dynamicSlots[i]);
            float ticks = dynamicValues[i].get(context);
            if (offsets && !dynamicVector[i]) applyOffsetSmoothing(target, ticks);
            else applySmoothing(target, ticks);
        }
        if (snap)
        {
            for (int slot : writtenSlots)
            {
                BoneTarget target = pose.get(slot);
                if (offsets)
                {
                    target.offsetMode = IVectorSink.Mode.SNAP;
                    continue;
                }
                target.snap = true;
                if (target.hasVector) target.vectorMode = IVectorSink.Mode.SNAP;
            }
        }
        if (defaultSmoothing != null)
        {
            for (int slot : writtenSlots)
            {
                if (offsets) applyOffsetSmoothing(pose.get(slot), defaultSmoothing);
                else applySmoothing(pose.get(slot), defaultSmoothing);
            }
        }
        for (int i = 0; i < smoothingSlots.length; i++)
        {
            if (offsets && !smoothingVector[i]) applyOffsetSmoothing(pose.get(smoothingSlots[i]), smoothingValues[i]);
            else applySmoothing(pose.get(smoothingSlots[i]), smoothingValues[i]);
        }
        for (int i = 0; i < modeSlots.length; i++)
        {
            if (offsets && !modeVector[i]) pose.get(modeSlots[i]).offsetMode = modes[i];
            else pose.get(modeSlots[i]).vectorMode = modes[i];
        }
    }

    /** As {@link #applySmoothing(BoneTarget, float[])}, for a part's offset (one smoothing, or one per axis). */
    public static void applyOffsetSmoothing(BoneTarget target, float[] value)
    {
        if (value.length >= 3)
        {
            if (!Float.isNaN(value[0])) target.offsetSmoothing.x = value[0];
            if (!Float.isNaN(value[1])) target.offsetSmoothing.y = value[1];
            if (!Float.isNaN(value[2])) target.offsetSmoothing.z = value[2];
        }
        else if (value.length == 1)
        {
            applyOffsetSmoothing(target, value[0]);
        }
    }

    /** One smoothing for every axis of a part's offset; NaN changes nothing. */
    public static void applyOffsetSmoothing(BoneTarget target, float ticks)
    {
        if (!Float.isNaN(ticks))
        {
            target.offsetSmoothing.set(ticks, ticks, ticks);
        }
    }

    public static void applySmoothing(BoneTarget target, float[] value)
    {
        if (value.length >= 3)
        {
            if (!Float.isNaN(value[0])) target.vectorSmoothing.x = value[0];
            if (!Float.isNaN(value[1])) target.vectorSmoothing.y = value[1];
            if (!Float.isNaN(value[2])) target.vectorSmoothing.z = value[2];
            if (!Float.isNaN(value[0])) target.smoothing = value[0];
        }
        else if (value.length == 1)
        {
            applySmoothing(target, value[0]);
        }
    }

    /** One smoothing for the rotation and every vector axis; NaN changes nothing. */
    public static void applySmoothing(BoneTarget target, float ticks)
    {
        if (!Float.isNaN(ticks))
        {
            target.smoothing = ticks;
            target.vectorSmoothing.set(ticks, ticks, ticks);
        }
    }

}
