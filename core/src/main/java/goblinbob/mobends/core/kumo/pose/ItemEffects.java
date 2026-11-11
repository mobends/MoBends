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
 * an entity bone's position vector; any other smooths and snaps both rotations and positions. A bone's
 * position is its vector on an entity bone ({@code root}, {@code center}) and its offset on a model part.
 */
public class ItemEffects
{

    /** Whether the effects go to the bones' positions alone rather than their rotations too. */
    private final boolean positions;
    private final Skeleton skeleton;
    /**
     * The bones the item writes, and what of them ({@link #ROTATION}, {@link #POSITION}): its effects
     * on them go to that alone (see {@link #writes}).
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
     * Says what the item writes of a bone: its snap, smoothing (but a smoothing given apart,
     * {"rotation": ..., "position": ...}) and vector mode then go to that alone, so an item that moves
     * the root doesn't snap or smooth the turn another item gives it, and a clip of a part's rotation
     * alone doesn't snap or smooth the offset another item gives it. Every bone the item writes is
     * told, before {@link #requireWritten}.
     */
    public void writes(int slot, boolean rotation, boolean position)
    {
        channelSlots = Arrays.copyOf(channelSlots, channelSlots.length + 1);
        channelMasks = Arrays.copyOf(channelMasks, channelMasks.length + 1);
        channelSlots[channelSlots.length - 1] = slot;
        channelMasks[channelMasks.length - 1] = (rotation ? ROTATION : 0) | (position ? POSITION : 0);
    }

    /** What the item writes of a bone ({@link #ROTATION}, {@link #POSITION}): an item that moves bones, its position alone; 0 for none. */
    private int channels(int slot)
    {
        int channels = 0;
        for (int i = 0; i < channelSlots.length; i++)
        {
            if (channelSlots[i] == slot) channels |= channelMasks[i];
        }
        return positions ? channels & POSITION : channels;
    }

    /**
     * Refuses a bone the item's {@code @smoothing} or {@code @vectorModes} names but the item doesn't
     * write: they say how what the item writes gets there, so another item's bone is smoothed where
     * it's written. {@code @vectorModes} names a bone whose position the item writes.
     *
     * @param item what the item is, for the message ({@code core:clip 'mobends:...'})
     */
    public void requireWritten(String item) throws MalformedKumoTemplateException
    {
        requireWritten(item, "@smoothing", smoothingSlots, BOTH);
        requireWritten(item, "@smoothing", dynamicSlots, BOTH);
        requireWritten(item, "@smoothing", rotationSlots, BOTH);
        requireWritten(item, "@smoothing", positionSlots, BOTH);
        requireWritten(item, "@vectorModes", modeSlots, POSITION);
    }

    private void requireWritten(String item, String key, int[] slots, int needed) throws MalformedKumoTemplateException
    {
        for (int slot : slots)
        {
            if ((channels(slot) & needed) == 0)
            {
                throw new MalformedKumoTemplateException(String.format("The %s of %s names '%s', %s: give it to the item that writes it.",
                        key, item, skeleton.nameOf(slot), needed == POSITION ? "whose position the item doesn't write" : "which the item doesn't write"));
            }
        }
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
                int channels = channels(slot);
                if ((channels & ROTATION) != 0) target.snap = true;
                if ((channels & POSITION) != 0)
                {
                    if (!skeleton.isEntityBone(slot)) target.offsetMode = IVectorSink.Mode.SNAP;
                    else if (target.hasVector) target.vectorMode = IVectorSink.Mode.SNAP;
                }
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
            int slot = modeSlots[i];
            if (skeleton.isEntityBone(slot)) pose.get(slot).vectorMode = modes[i];
            else pose.get(slot).offsetMode = modes[i];
        }
    }

    /**
     * Smooths what the item writes of a bone (see {@link #channels}): its rotation, its position (a
     * part's offset, an entity bone's vector), or both.
     */
    private void smooth(Pose pose, int slot, float[] value)
    {
        BoneTarget target = pose.get(slot);
        int channels = channels(slot);
        if ((channels & ROTATION) != 0) applyRotationSmoothing(target, value);
        if ((channels & POSITION) != 0)
        {
            if (skeleton.isEntityBone(slot)) applyVectorSmoothing(target, value);
            else applyOffsetSmoothing(target, value);
        }
    }

    /** As {@link #smooth(Pose, int, float[])}, with one smoothing for every channel; NaN changes nothing. */
    private void smooth(Pose pose, int slot, float ticks)
    {
        if (Float.isNaN(ticks)) return;
        BoneTarget target = pose.get(slot);
        int channels = channels(slot);
        if ((channels & ROTATION) != 0) target.smoothing = ticks;
        if ((channels & POSITION) != 0)
        {
            if (skeleton.isEntityBone(slot)) target.vectorSmoothing.set(ticks, ticks, ticks);
            else applyOffsetSmoothing(target, ticks);
        }
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
