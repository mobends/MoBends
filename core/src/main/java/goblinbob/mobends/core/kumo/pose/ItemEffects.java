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

/** Damping rates, vector modes and snapping attached to one pose item, applied to the bones the item wrote. */
public class ItemEffects
{

    private final int[] dampingSlots;
    private final float[][] dampingValues;
    private final float[] defaultDamping;
    private final int[] modeSlots;
    private final IVectorSink.Mode[] modes;
    private final boolean snap;
    private final int[] dynamicSlots;
    private final Expression[] dynamicValues;

    /** @param scope Where the damping's expressions are compiled. */
    public ItemEffects(ExpressionScope scope, Skeleton skeleton, DampingTemplate damping, Map<String, IVectorSink.Mode> vectorModes, boolean snap) throws MalformedKumoTemplateException
    {
        this.snap = snap;
        List<Integer> slots = new ArrayList<>();
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
                    slots.add(skeleton.indexOf(entry.getKey()));
                    values.add(entry.getValue());
                }
            }
        }
        dampingSlots = new int[slots.size()];
        dampingValues = new float[slots.size()][];
        for (int i = 0; i < dampingSlots.length; i++)
        {
            dampingSlots[i] = slots.get(i);
            dampingValues[i] = values.get(i);
        }
        defaultDamping = fallback;

        List<Integer> dynSlots = new ArrayList<>();
        List<Expression> dynValues = new ArrayList<>();
        if (damping != null)
        {
            for (Map.Entry<String, ExpressionTemplate> entry : damping.dynamic.entrySet())
            {
                dynSlots.add(skeleton.indexOf(entry.getKey()));
                dynValues.add(Expression.compile(entry.getValue(), scope, Expression.ONE));
            }
        }
        dynamicSlots = new int[dynSlots.size()];
        dynamicValues = new Expression[dynSlots.size()];
        for (int k = 0; k < dynamicSlots.length; k++)
        {
            dynamicSlots[k] = dynSlots.get(k);
            dynamicValues[k] = dynValues.get(k);
        }

        modeSlots = new int[vectorModes == null ? 0 : vectorModes.size()];
        modes = new IVectorSink.Mode[modeSlots.length];
        int i = 0;
        if (vectorModes != null)
        {
            for (Map.Entry<String, IVectorSink.Mode> entry : vectorModes.entrySet())
            {
                modeSlots[i] = skeleton.indexOf(entry.getKey());
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
            applyDamping(pose.get(dynamicSlots[i]), dynamicValues[i].get(context));
        }
        if (snap)
        {
            for (int slot : writtenSlots)
            {
                BoneTarget target = pose.get(slot);
                target.snap = true;
                if (target.hasVector) target.vectorMode = IVectorSink.Mode.SNAP;
            }
        }
        if (defaultDamping != null)
        {
            for (int slot : writtenSlots)
            {
                applyDamping(pose.get(slot), defaultDamping);
            }
        }
        for (int i = 0; i < dampingSlots.length; i++)
        {
            applyDamping(pose.get(dampingSlots[i]), dampingValues[i]);
        }
        for (int i = 0; i < modeSlots.length; i++)
        {
            pose.get(modeSlots[i]).vectorMode = modes[i];
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
