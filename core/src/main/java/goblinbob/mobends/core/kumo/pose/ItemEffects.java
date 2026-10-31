package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.bind.IVectorSink;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.DampingTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Damping rates, vector modes and snapping attached to one pose item, applied to the bones the item wrote.
 * An item that moves parts ({@code core:translate} on a part) damps and snaps their position offsets;
 * any other damps and snaps rotations, and the entity-level vectors.
 */
public class ItemEffects
{

    /** Whether the effects go to the parts' offsets rather than their rotations. */
    private final boolean offsets;
    private final int[] dampingSlots;
    /** Per entry of {@link #dampingSlots} / {@link #modeSlots} / {@link #dynamicSlots}: an entity-level vector bone. */
    private final boolean[] dampingVector;
    private final float[][] dampingValues;
    private final float[] defaultDamping;
    private final int[] modeSlots;
    private final boolean[] modeVector;
    private final IVectorSink.Mode[] modes;
    private final boolean snap;
    private final int[] dynamicSlots;
    private final boolean[] dynamicVector;
    private final Expression[] dynamicValues;

    /** @param scope Where the damping's expressions are compiled. */
    public ItemEffects(ExpressionScope scope, Skeleton skeleton, DampingTemplate damping, Map<String, IVectorSink.Mode> vectorModes, boolean snap) throws MalformedKumoTemplateException
    {
        this(scope, skeleton, damping, vectorModes, snap, false);
    }

    /**
     * @param scope   Where the damping's expressions are compiled.
     * @param offsets Whether the item moves parts: its effects go to their offsets (the bones it
     *                writes and the parts it names; the vector bones it names are damped as ever).
     */
    public ItemEffects(ExpressionScope scope, Skeleton skeleton, DampingTemplate damping, Map<String, IVectorSink.Mode> vectorModes, boolean snap, boolean offsets) throws MalformedKumoTemplateException
    {
        this.snap = snap;
        this.offsets = offsets;
        List<String> names = new ArrayList<>();
        List<float[]> values = new ArrayList<>();
        float[] fallback = null;
        if (damping != null)
        {
            for (Map.Entry<String, float[]> entry : damping.entries.entrySet())
            {
                if (DampingTemplate.DEFAULT.equals(entry.getKey()))
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
        dampingSlots = new int[names.size()];
        dampingVector = new boolean[names.size()];
        dampingValues = new float[names.size()][];
        for (int i = 0; i < dampingSlots.length; i++)
        {
            dampingSlots[i] = skeleton.indexOf(names.get(i));
            dampingVector[i] = Skeleton.isVectorBone(names.get(i));
            dampingValues[i] = values.get(i);
        }
        defaultDamping = fallback;

        List<String> dynNames = new ArrayList<>();
        List<Expression> dynValues = new ArrayList<>();
        if (damping != null)
        {
            for (Map.Entry<String, ExpressionTemplate> entry : damping.dynamic.entrySet())
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
        return dampingSlots.length == 0 && defaultDamping == null && modeSlots.length == 0 && !snap && dynamicSlots.length == 0;
    }

    /** @param writtenSlots the slots the item wrote this frame (the default rate applies to those). */
    public void apply(Pose pose, int[] writtenSlots, ITriggerConditionContext context)
    {
        for (int i = 0; i < dynamicSlots.length; i++)
        {
            BoneTarget target = pose.get(dynamicSlots[i]);
            float rate = dynamicValues[i].get(context);
            if (offsets && !dynamicVector[i]) applyOffsetDamping(target, rate);
            else applyDamping(target, rate);
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
        if (defaultDamping != null)
        {
            for (int slot : writtenSlots)
            {
                if (offsets) applyOffsetDamping(pose.get(slot), defaultDamping);
                else applyDamping(pose.get(slot), defaultDamping);
            }
        }
        for (int i = 0; i < dampingSlots.length; i++)
        {
            if (offsets && !dampingVector[i]) applyOffsetDamping(pose.get(dampingSlots[i]), dampingValues[i]);
            else applyDamping(pose.get(dampingSlots[i]), dampingValues[i]);
        }
        for (int i = 0; i < modeSlots.length; i++)
        {
            if (offsets && !modeVector[i]) pose.get(modeSlots[i]).offsetMode = modes[i];
            else pose.get(modeSlots[i]).vectorMode = modes[i];
        }
    }

    /** As {@link #applyDamping(BoneTarget, float[])}, for a part's offset (one rate, or one per axis). */
    public static void applyOffsetDamping(BoneTarget target, float[] value)
    {
        if (value.length >= 3)
        {
            if (!Float.isNaN(value[0])) target.offsetSmoothness.x = value[0];
            if (!Float.isNaN(value[1])) target.offsetSmoothness.y = value[1];
            if (!Float.isNaN(value[2])) target.offsetSmoothness.z = value[2];
        }
        else if (value.length == 1)
        {
            applyOffsetDamping(target, value[0]);
        }
    }

    /** One rate for every axis of a part's offset; NaN changes nothing. */
    public static void applyOffsetDamping(BoneTarget target, float rate)
    {
        if (!Float.isNaN(rate))
        {
            target.offsetSmoothness.set(rate, rate, rate);
        }
    }

    public static void applyDamping(BoneTarget target, float[] value)
    {
        if (value.length >= 3)
        {
            if (!Float.isNaN(value[0])) target.vectorSmoothness.x = value[0];
            if (!Float.isNaN(value[1])) target.vectorSmoothness.y = value[1];
            if (!Float.isNaN(value[2])) target.vectorSmoothness.z = value[2];
            if (!Float.isNaN(value[0])) target.smoothness = value[0];
        }
        else if (value.length == 1)
        {
            applyDamping(target, value[0]);
        }
    }

    /** One rate for the rotation and every vector axis; NaN changes nothing. */
    public static void applyDamping(BoneTarget target, float rate)
    {
        if (!Float.isNaN(rate))
        {
            target.smoothness = rate;
            target.vectorSmoothness.set(rate, rate, rate);
        }
    }

}
