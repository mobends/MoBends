package goblinbob.mobends.core.kumo.state.node;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.bind.IVectorSink;
import goblinbob.mobends.core.kumo.driver.DriverRegistry;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.pose.*;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.StateLayout;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.*;
import goblinbob.mobends.core.kumo.state.template.pose.ClipItemTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.PoseItemTemplate;
import goblinbob.mobends.core.math.vector.Vec3f;

import java.util.*;

/**
 * A node of a layer: an ordered stack of pose items (clips, drivers), a smoothing table, a list of
 * bones to snap on entry. Fallthrough and vanilla nodes are pose nodes without items. (Its
 * connections belong to the layer, see {@code LayerState}.)
 */
public class PoseNode implements INodeState
{

    private final String name;
    private final List<IPoseItem> items;
    private final int[] smoothingSlots;
    private final float[][] smoothingValues;
    private final int[] snapSlots;
    private final List<IPoseItem> enterItems;
    private final Skeleton skeleton;
    /** The stateful expressions of the node's items, started over when the node starts. */
    private Expression[] held = new Expression[0];

    private boolean fallthrough;
    private boolean vanilla;

    /** Its state (see StateLayout): its clock, whether the snaps and the enter pose are still to apply. */
    private final int clockSlot, snapPendingSlot, enterPendingSlot;
    /** The pose its enter items make when it starts, or -1 without any. */
    private final int enterPose;

    public PoseNode(String name, List<IPoseItem> items, List<IPoseItem> enterItems, Skeleton skeleton, SmoothingTemplate layerSmoothing, SmoothingTemplate nodeSmoothing,
                    List<String> snapOnEnter, StateLayout layout)
    {
        this.name = name;
        this.clockSlot = layout.floats(3, 0);
        this.snapPendingSlot = clockSlot + 1;
        this.enterPendingSlot = clockSlot + 2;
        this.enterPose = enterItems == null || enterItems.isEmpty() ? -1 : layout.pose();
        this.items = items;
        this.enterItems = enterItems == null ? Collections.<IPoseItem>emptyList() : enterItems;
        this.skeleton = skeleton;

        // Merge layer defaults with node overrides into slot-indexed tables.
        Map<String, float[]> smoothing = new LinkedHashMap<>();
        if (layerSmoothing != null) smoothing.putAll(layerSmoothing.entries);
        if (nodeSmoothing != null) smoothing.putAll(nodeSmoothing.entries);
        float[] defaultSmoothing = smoothing.remove(SmoothingTemplate.DEFAULT);
        this.smoothingValues = new float[smoothing.size() + (defaultSmoothing != null ? 1 : 0)][];
        this.smoothingSlots = new int[smoothingValues.length];
        int i = 0;
        for (Map.Entry<String, float[]> entry : smoothing.entrySet())
        {
            smoothingSlots[i] = skeleton.indexOf(entry.getKey());
            smoothingValues[i] = entry.getValue();
            i++;
        }
        if (defaultSmoothing != null)
        {
            smoothingSlots[i] = -1;
            smoothingValues[i] = defaultSmoothing;
        }

        this.snapSlots = new int[snapOnEnter == null ? 0 : snapOnEnter.size()];
        for (int j = 0; j < snapSlots.length; j++)
        {
            snapSlots[j] = skeleton.indexOf(snapOnEnter.get(j));
        }
    }

    // --- factories -------------------------------------------------------------------------------

    public static PoseNode createPose(IKumoInstancingContext context, Skeleton skeleton, LayerTemplate layer, PoseNodeTemplate template) throws MalformedKumoTemplateException
    {
        // What the node's items remember starts over when the node starts.
        List<Expression> held = new ArrayList<>();
        List<IPoseItem> items = new ArrayList<>();
        List<IPoseItem> enterItems = new ArrayList<>();
        context.getExpressionScope().beginHolding(held);
        try
        {
            LayerMirror mirror = layer.mirror == null ? null : new LayerMirror(skeleton, layer.mirror, context.getExpressionScope());
            if (template.pose != null)
            {
                for (PoseItemTemplate itemTemplate : template.pose)
                {
                    items.add(createItem(context, skeleton, mirror, itemTemplate));
                }
            }
            if (template.enterPose != null)
            {
                for (PoseItemTemplate itemTemplate : template.enterPose)
                {
                    enterItems.add(createItem(context, skeleton, mirror, itemTemplate));
                }
            }
        }
        finally
        {
            context.getExpressionScope().endHolding();
        }
        PoseNode node = new PoseNode(template.name, items, enterItems, skeleton, layer.smoothing, template.smoothing, template.snapOnEnter,
                context.getExpressionScope().getLayout());
        node.held = held.toArray(new Expression[0]);
        return node;
    }

    public static PoseNode createFallthrough(IKumoInstancingContext context, Skeleton skeleton, LayerTemplate layer, FallthroughNodeTemplate template) throws MalformedKumoTemplateException
    {
        template.validate();
        PoseNode node = new PoseNode(template.name, Collections.<IPoseItem>emptyList(), null, skeleton, null, null, null, context.getExpressionScope().getLayout());
        node.fallthrough = true;
        return node;
    }

    public static PoseNode createVanilla(IKumoInstancingContext context, Skeleton skeleton, LayerTemplate layer, VanillaNodeTemplate template) throws MalformedKumoTemplateException
    {
        template.validate();
        PoseNode node = new PoseNode(template.name, Collections.<IPoseItem>emptyList(), null, skeleton, null, null, null, context.getExpressionScope().getLayout());
        node.vanilla = true;
        return node;
    }

    private static IPoseItem createItem(IKumoInstancingContext context, Skeleton skeleton, LayerMirror mirror, PoseItemTemplate template) throws MalformedKumoTemplateException
    {
        IPoseItem item = createPlainItem(context, skeleton, template);
        if (template.mirror || template.swapSides)
        {
            if (mirror == null)
            {
                throw new MalformedKumoTemplateException("An item sets \"mirror\" / \"swapSides\" but its layer has no \"mirror\" rule.");
            }
            item = new MirroredPoseItem(item, mirror, skeleton, template.mirror);
        }
        return item;
    }

    private static IPoseItem createPlainItem(IKumoInstancingContext context, Skeleton skeleton, PoseItemTemplate template) throws MalformedKumoTemplateException
    {
        IPoseItem item = createUnconditionalItem(context, skeleton, template);
        // The item is skipped while its condition does not hold.
        if (template.when == null)
        {
            return item;
        }
        return new ConditionalPoseItem(item, Expression.compileCondition(template.when, context.getExpressionScope()));
    }

    private static IPoseItem createUnconditionalItem(IKumoInstancingContext context, Skeleton skeleton, PoseItemTemplate template) throws MalformedKumoTemplateException
    {
        ItemEffects effects = new ItemEffects(context.getExpressionScope(), skeleton, template.smoothing, template.vectorModes, template.snap);

        if (template instanceof ClipItemTemplate)
        {
            ClipItemTemplate clipTemplate = (ClipItemTemplate) template;
            KeyframeAnimation animation = requireAnimation(context, clipTemplate.animationKey);
            if (animation.interpolation != null && !"linear".equals(animation.interpolation) && !"step".equals(animation.interpolation))
            {
                throw new MalformedKumoTemplateException(String.format("The clip '%s' has the interpolation '%s': it is \"linear\" or \"step\".",
                        clipTemplate.animationKey, animation.interpolation));
            }
            ClipBinding binding = new ClipBinding(animation, skeleton, clipTemplate.bones);

            float clipLength = animation.duration != null ? animation.duration : Math.max(binding.keyframeCount - 1, 0);
            Map<String, Expression> clipValues = new HashMap<>();
            clipValues.put("clipLength", Expression.constant(clipLength));
            float duration = Float.NaN;
            if (clipTemplate.duration != null)
            {
                duration = clipTemplate.duration;
                if (!(duration >= 0))
                {
                    throw new MalformedKumoTemplateException("The duration of a clip can't be negative: " + clipTemplate.animationKey);
                }
                clipValues.put("clipDuration", Expression.constant(duration));
            }
            Expression frame = clipTemplate.frame == null ? null
                    : Expression.compile(clipTemplate.frame.json, context.getExpressionScope().withValues(clipValues));

            return new ClipPoseItem(binding, frame,
                    Expression.compile(clipTemplate.weight, context.getExpressionScope(), Expression.ONE),
                    binding.spaces(clipTemplate.space),
                    clipLength, duration, effects.isEmpty() ? null : effects);
        }

        if (template instanceof DriverItemTemplate)
        {
            return DriverRegistry.INSTANCE.create(context, skeleton, (DriverItemTemplate) template);
        }

        throw new MalformedKumoTemplateException("Unknown pose item template: " + template.getClass().getName());
    }

    private static KeyframeAnimation requireAnimation(IKumoInstancingContext context, String key) throws MalformedKumoTemplateException
    {
        KeyframeAnimation animation = context.getAnimation(key);
        if (animation == null)
        {
            throw new MalformedKumoTemplateException(String.format("Trying to use a missing animation: \"%s\".", key));
        }
        if (animation.bones == null)
        {
            throw new MalformedKumoTemplateException(String.format("Animation \"%s\" has no bones.", key));
        }
        return animation;
    }

    // --- INodeState ------------------------------------------------------------------------------

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public float getElapsedTicks(ITriggerConditionContext context)
    {
        return context.getState().floats[clockSlot];
    }

    @Override
    public boolean isVanilla()
    {
        return vanilla;
    }

    @Override
    public boolean isFallthrough()
    {
        return fallthrough;
    }

    /**
     * Once every timed item (a clip with a {@code duration}) has run its course. A node without
     * items (fallthrough, vanilla) is always finished; one whose items are all untimed never is.
     */
    @Override
    public boolean isAnimationFinished(ITriggerConditionContext context)
    {
        float elapsed = getElapsedTicks(context);
        boolean anyTimed = false;
        for (IPoseItem item : items)
        {
            float duration = item.getDuration();
            if (!Float.isNaN(duration))
            {
                if (elapsed < duration)
                {
                    return false;
                }
                anyTimed = true;
            }
        }
        return anyTimed || items.isEmpty();
    }

    @Override
    public void start(IKumoContext context) throws MalformedKumoTemplateException
    {
        float[] state = context.getState().floats;
        state[clockSlot] = 0;
        state[snapPendingSlot] = snapSlots.length > 0 ? 1 : 0;
        for (Expression expression : held)
        {
            expression.restart(context);
        }
        for (IPoseItem item : items)
        {
            item.onNodeStarted(context);
        }
        if (enterPose >= 0)
        {
            Pose entered = context.getState().poses[enterPose];
            entered.clear();
            for (IPoseItem item : enterItems)
            {
                item.onNodeStarted(context);
                item.apply(entered, context, 0);
            }
            state[enterPendingSlot] = 1;
        }
    }

    @Override
    public void evaluate(IKumoContext context, Pose pose) throws MalformedKumoTemplateException
    {
        float[] state = context.getState().floats;
        for (IPoseItem item : items)
        {
            item.apply(pose, context, state[clockSlot]);
        }

        // Node-level smoothing applies to whatever this node wrote and no item smoothed already.
        for (int i = 0; i < smoothingSlots.length; i++)
        {
            if (smoothingSlots[i] >= 0)
            {
                BoneTarget target = pose.get(smoothingSlots[i]);
                if (Float.isNaN(target.smoothing) && isUnsmoothed(target.vectorSmoothing))
                {
                    ItemEffects.applySmoothing(target, smoothingValues[i]);
                }
            }
            else
            {
                for (int slot = 0; slot < pose.size(); slot++)
                {
                    BoneTarget target = pose.get(slot);
                    if ((target.hasRotation || target.hasPre || target.hasPost || target.hasVector) && Float.isNaN(target.smoothing) && isUnsmoothed(target.vectorSmoothing))
                    {
                        ItemEffects.applySmoothing(target, smoothingValues[i]);
                    }
                }
            }
        }

        if (state[enterPendingSlot] != 0)
        {
            Pose enterPose = context.getState().poses[this.enterPose];
            for (int slot = 0; slot < pose.size(); slot++)
            {
                BoneTarget entered = enterPose.get(slot);
                BoneTarget target = pose.get(slot);
                if (entered.hasRotation)
                {
                    if (target.hasRotation || target.hasPre || target.hasPost)
                    {
                        target.hasSnapFrom = true;
                        target.snapFrom.set(entered.rotation);
                    }
                    else
                    {
                        target.hasRotation = true;
                        target.rotation.set(entered.rotation);
                        target.snap = true;
                    }
                }
                if (entered.hasVector)
                {
                    if (target.hasVector)
                    {
                        target.hasVectorStart = true;
                        target.vectorStart.set(entered.vector);
                    }
                    else
                    {
                        target.hasVector = true;
                        target.vector.set(entered.vector);
                        target.vectorAdditive = entered.vectorAdditive;
                        target.vectorMode = IVectorSink.Mode.SNAP;
                    }
                }
            }
            state[enterPendingSlot] = 0;
        }

        if (state[snapPendingSlot] != 0)
        {
            for (int slot : snapSlots)
            {
                BoneTarget target = pose.get(slot);
                target.snap = true;
                target.vectorMode = IVectorSink.Mode.SNAP;
                target.offsetMode = IVectorSink.Mode.SNAP;
            }
            state[snapPendingSlot] = 0;
        }

        for (int slot = 0; slot < pose.size(); slot++)
        {
            BoneTarget target = pose.get(slot);
            if (target.hasVector && isUnsmoothed(target.vectorSmoothing) && target.vectorMode == IVectorSink.Mode.RETARGET)
            {
                // Keyframed root motion is already smooth; without an explicit smoothing entry it is applied as is.
                target.vectorMode = IVectorSink.Mode.SNAP;
            }
        }
    }

    private static boolean isUnsmoothed(Vec3f smoothing)
    {
        return Float.isNaN(smoothing.x) && Float.isNaN(smoothing.y) && Float.isNaN(smoothing.z);
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
        context.getState().floats[clockSlot] += deltaTime;
        for (IPoseItem item : items)
        {
            item.advance(context, deltaTime);
        }
    }

}
