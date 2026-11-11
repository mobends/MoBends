package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.bind.IVectorSink;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.SmoothingTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Smoothing (in ticks), vector modes and snapping attached to one pose item, applied to the bones the item wrote.
 * An item that moves bones ({@code core:translate}) smooths and snaps their positions: a part's offset,
 * an entity bone's position vector; any other smooths and snaps both rotations and positions.
 */
public class ItemEffects
{

    /** Whether the effects go to the bones' positions alone rather than their rotations too. */
    private final boolean positions;
    private final Skeleton skeleton;
    /**
     * The entity's own bones the item writes one channel of, and which ({@link #ROTATION},
     * {@link #POSITION}): its effects on them go to that channel alone (see {@link #writes}). Any
     * other bone's go to both.
     */
    private int[] channelSlots = new int[0];
    private int[] channelMasks = new int[0];

    private static final int ROTATION = 1, POSITION = 2, BOTH = ROTATION | POSITION;
    private final int[] smoothingSlots;
    private final float[][] smoothingValues;
    /** A bone's rotation's and position's smoothing given apart (see {@link SmoothingTemplate#rotations}). */
    private final int[] rotationSlots, positionSlots;
    private final float[][] rotationValues, positionValues;
    private final float[] defaultSmoothing;
    private final int[] modeSlots;
    private final IVectorSink.Mode[] modes;
    private final boolean snap;
    private final int[] dynamicSlots;
    private final Expression[] dynamicValues;
    /** An {@code @default} given as an expression: evaluated every frame, for every slot the item writes. */
    private final Expression dynamicDefault;

    /** @param scope Where the smoothing's expressions are compiled. */
    public ItemEffects(ExpressionScope scope, Skeleton skeleton, SmoothingTemplate smoothing, Map<String, IVectorSink.Mode> vectorModes, boolean snap) throws MalformedKumoTemplateException
    {
        this(scope, skeleton, smoothing, vectorModes, snap, false);
    }

    /**
     * @param scope   Where the smoothing's expressions are compiled.
     * @param positions Whether the item moves bones: its effects go to their positions alone (a
     *                  part's offset, an entity bone's position vector), not their rotations.
     */
    public ItemEffects(ExpressionScope scope, Skeleton skeleton, SmoothingTemplate smoothing, Map<String, IVectorSink.Mode> vectorModes, boolean snap, boolean positions) throws MalformedKumoTemplateException
    {
        this.snap = snap;
        this.positions = positions;
        this.skeleton = skeleton;
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
        smoothingValues = new float[names.size()][];
        for (int i = 0; i < smoothingSlots.length; i++)
        {
            smoothingSlots[i] = skeleton.indexOf(names.get(i));
            smoothingValues[i] = values.get(i);
        }
        defaultSmoothing = fallback;

        int rotationCount = smoothing == null ? 0 : smoothing.rotations.size();
        rotationSlots = new int[rotationCount];
        rotationValues = new float[rotationCount][];
        if (smoothing != null)
        {
            int k = 0;
            for (Map.Entry<String, float[]> entry : smoothing.rotations.entrySet())
            {
                rotationSlots[k] = skeleton.indexOf(entry.getKey());
                rotationValues[k++] = entry.getValue();
            }
        }
        int positionCount = smoothing == null ? 0 : smoothing.positions.size();
        positionSlots = new int[positionCount];
        positionValues = new float[positionCount][];
        if (smoothing != null)
        {
            int k = 0;
            for (Map.Entry<String, float[]> entry : smoothing.positions.entrySet())
            {
                positionSlots[k] = skeleton.indexOf(entry.getKey());
                positionValues[k++] = entry.getValue();
            }
        }

        List<String> dynNames = new ArrayList<>();
        List<Expression> dynValues = new ArrayList<>();
        Expression dynFallback = null;
        if (smoothing != null)
        {
            for (Map.Entry<String, ExpressionTemplate> entry : smoothing.dynamic.entrySet())
            {
                Expression value = Expression.compile(entry.getValue(), scope, Expression.ONE);
                if (SmoothingTemplate.DEFAULT.equals(entry.getKey()))
                {
                    dynFallback = value;
                }
                else
                {
                    dynNames.add(entry.getKey());
                    dynValues.add(value);
                }
            }
        }
        dynamicDefault = dynFallback;
        dynamicSlots = new int[dynNames.size()];
        dynamicValues = new Expression[dynNames.size()];
        for (int k = 0; k < dynamicSlots.length; k++)
        {
            dynamicSlots[k] = skeleton.indexOf(dynNames.get(k));
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

    /**
     * Says what the item writes of one of the entity's own bones: its snap and smoothing (but a
     * smoothing given apart, {"rotation": ..., "position": ...}) then go to that alone, so an item
     * that moves the root doesn't snap or smooth the turn another item gives it. A bone it is never
     * told about gets them on both.
     */
    public void writes(int slot, boolean rotation, boolean position)
    {
        if (!skeleton.isEntityBone(slot) || (rotation && position))
        {
            return;
        }
        channelSlots = Arrays.copyOf(channelSlots, channelSlots.length + 1);
        channelMasks = Arrays.copyOf(channelMasks, channelMasks.length + 1);
        channelSlots[channelSlots.length - 1] = slot;
        channelMasks[channelMasks.length - 1] = (rotation ? ROTATION : 0) | (position ? POSITION : 0);
    }

    private int channels(int slot)
    {
        for (int i = 0; i < channelSlots.length; i++)
        {
            if (channelSlots[i] == slot) return channelMasks[i];
        }
        return BOTH;
    }

    public boolean isEmpty()
    {
        return smoothingSlots.length == 0 && defaultSmoothing == null && dynamicDefault == null && modeSlots.length == 0 && !snap && dynamicSlots.length == 0
                && rotationSlots.length == 0 && positionSlots.length == 0;
    }

    /** @param writtenSlots the slots the item wrote this frame (the default smoothing applies to those). */
    public void apply(Pose pose, int[] writtenSlots, ITriggerConditionContext context)
    {
        if (snap)
        {
            for (int slot : writtenSlots)
            {
                BoneTarget target = pose.get(slot);
                if (positions && !skeleton.isEntityBone(slot))
                {
                    target.offsetMode = IVectorSink.Mode.SNAP;
                    continue;
                }
                int channels = positions ? POSITION : channels(slot);
                if ((channels & ROTATION) != 0) target.snap = true;
                if ((channels & POSITION) != 0 && target.hasVector) target.vectorMode = IVectorSink.Mode.SNAP;
            }
        }
        if (defaultSmoothing != null)
        {
            for (int slot : writtenSlots)
            {
                smooth(pose, slot, defaultSmoothing);
            }
        }
        if (dynamicDefault != null)
        {
            float ticks = dynamicDefault.get(context);
            for (int slot : writtenSlots)
            {
                smooth(pose, slot, ticks);
            }
        }
        // A bone's own smoothing, numbers or expression, wins over the default.
        for (int i = 0; i < smoothingSlots.length; i++)
        {
            smooth(pose, smoothingSlots[i], smoothingValues[i]);
        }
        for (int i = 0; i < dynamicSlots.length; i++)
        {
            smooth(pose, dynamicSlots[i], dynamicValues[i].get(context));
        }
        // Given apart: an item that moves bones smooths no rotation.
        if (!positions)
        {
            for (int i = 0; i < rotationSlots.length; i++)
            {
                applyRotationSmoothing(pose.get(rotationSlots[i]), rotationValues[i]);
            }
        }
        for (int i = 0; i < positionSlots.length; i++)
        {
            BoneTarget target = pose.get(positionSlots[i]);
            if (skeleton.isEntityBone(positionSlots[i])) applyVectorSmoothing(target, positionValues[i]);
            else applyOffsetSmoothing(target, positionValues[i]);
        }
        for (int i = 0; i < modeSlots.length; i++)
        {
            if (positions && !skeleton.isEntityBone(modeSlots[i])) pose.get(modeSlots[i]).offsetMode = modes[i];
            else pose.get(modeSlots[i]).vectorMode = modes[i];
        }
    }

    /**
     * Smooths what the item moves of a bone: a part's offset or an entity bone's position (an item
     * that moves bones), what it writes of an entity bone (see {@link #writes}), or else everything.
     */
    private void smooth(Pose pose, int slot, float[] value)
    {
        BoneTarget target = pose.get(slot);
        if (positions && !skeleton.isEntityBone(slot))
        {
            applyOffsetSmoothing(target, value);
            return;
        }
        int channels = positions ? POSITION : channels(slot);
        if (channels == BOTH) applySmoothing(target, value);
        else if (channels == ROTATION) applyRotationSmoothing(target, value);
        else applyVectorSmoothing(target, value);
    }

    /** As {@link #smooth(Pose, int, float[])}, with one smoothing for every channel; NaN changes nothing. */
    private void smooth(Pose pose, int slot, float ticks)
    {
        if (Float.isNaN(ticks)) return;
        BoneTarget target = pose.get(slot);
        if (positions && !skeleton.isEntityBone(slot))
        {
            applyOffsetSmoothing(target, ticks);
            return;
        }
        int channels = positions ? POSITION : channels(slot);
        if ((channels & ROTATION) != 0) target.smoothing = ticks;
        if ((channels & POSITION) != 0) target.vectorSmoothing.set(ticks, ticks, ticks);
    }

    /** As {@link #applySmoothing(BoneTarget, float[])}, for the rotation alone (its first value). */
    public static void applyRotationSmoothing(BoneTarget target, float[] value)
    {
        if (value.length >= 1 && !Float.isNaN(value[0]))
        {
            target.smoothing = value[0];
        }
    }

    /** As {@link #applySmoothing(BoneTarget, float[])}, for an entity bone's position alone. */
    public static void applyVectorSmoothing(BoneTarget target, float[] value)
    {
        if (value.length >= 3)
        {
            if (!Float.isNaN(value[0])) target.vectorSmoothing.x = value[0];
            if (!Float.isNaN(value[1])) target.vectorSmoothing.y = value[1];
            if (!Float.isNaN(value[2])) target.vectorSmoothing.z = value[2];
        }
        else if (value.length == 1 && !Float.isNaN(value[0]))
        {
            target.vectorSmoothing.set(value[0], value[0], value[0]);
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
