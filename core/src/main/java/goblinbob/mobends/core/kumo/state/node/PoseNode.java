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
import goblinbob.mobends.core.kumo.state.template.*;
import goblinbob.mobends.core.kumo.state.template.pose.ClipItemTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.PoseItemTemplate;
import goblinbob.mobends.core.math.vector.Vec3f;

import java.util.*;

/**
 * A node of a layer: an ordered stack of pose items (clips, drivers), a damping table, a list of
 * bones to snap on entry. Fallthrough and vanilla nodes are pose nodes without items. (Its
 * connections belong to the layer, see {@code LayerState}.)
 */
public class PoseNode implements INodeState
{

    private final String name;
    private final List<IPoseItem> items;
    private final int[] dampingSlots;
    private final float[][] dampingValues;
    private final int[] snapSlots;
    private final List<IPoseItem> enterItems;
    private final Skeleton skeleton;
    /** The stateful expressions of the node's items, started over when the node starts. */
    private Expression[] held = new Expression[0];
    private Pose enterPose;
    private boolean enterPending;

    private boolean fallthrough;
    private boolean vanilla;

    private float elapsed;
    private boolean snapPending;

    public PoseNode(String name, List<IPoseItem> items, List<IPoseItem> enterItems, Skeleton skeleton, DampingTemplate layerDamping, DampingTemplate nodeDamping, List<String> snapOnEnter)
    {
        this.name = name;
        this.items = items;
        this.enterItems = enterItems == null ? Collections.<IPoseItem>emptyList() : enterItems;
        this.skeleton = skeleton;

        // Merge layer defaults with node overrides into slot-indexed tables.
        Map<String, float[]> damping = new LinkedHashMap<>();
        if (layerDamping != null) damping.putAll(layerDamping.entries);
        if (nodeDamping != null) damping.putAll(nodeDamping.entries);
        float[] defaultDamping = damping.remove(DampingTemplate.DEFAULT);
        this.dampingValues = new float[damping.size() + (defaultDamping != null ? 1 : 0)][];
        this.dampingSlots = new int[dampingValues.length];
        int i = 0;
        for (Map.Entry<String, float[]> entry : damping.entrySet())
        {
            dampingSlots[i] = skeleton.indexOf(entry.getKey());
            dampingValues[i] = entry.getValue();
            i++;
        }
        if (defaultDamping != null)
        {
            dampingSlots[i] = -1;
            dampingValues[i] = defaultDamping;
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
            LayerSpaces spaces = new LayerSpaces(skeleton, layer, context.getExpressionScope());
            if (template.pose != null)
            {
                for (PoseItemTemplate itemTemplate : template.pose)
                {
                    items.add(createItem(context, skeleton, spaces, itemTemplate));
                }
            }
            if (template.enterPose != null)
            {
                for (PoseItemTemplate itemTemplate : template.enterPose)
                {
                    enterItems.add(createItem(context, skeleton, spaces, itemTemplate));
                }
            }
        }
        finally
        {
            context.getExpressionScope().endHolding();
        }
        PoseNode node = new PoseNode(template.name, items, enterItems, skeleton, layer.damping, template.damping, template.snapOnEnter);
        node.held = held.toArray(new Expression[0]);
        return node;
    }

    public static PoseNode createFallthrough(IKumoInstancingContext context, Skeleton skeleton, LayerTemplate layer, FallthroughNodeTemplate template) throws MalformedKumoTemplateException
    {
        template.validate();
        PoseNode node = new PoseNode(template.name, Collections.<IPoseItem>emptyList(), null, skeleton, null, null, null);
        node.fallthrough = true;
        return node;
    }

    public static PoseNode createVanilla(IKumoInstancingContext context, Skeleton skeleton, LayerTemplate layer, VanillaNodeTemplate template) throws MalformedKumoTemplateException
    {
        template.validate();
        PoseNode node = new PoseNode(template.name, Collections.<IPoseItem>emptyList(), null, skeleton, null, null, null);
        node.vanilla = true;
        return node;
    }

    private static IPoseItem createItem(IKumoInstancingContext context, Skeleton skeleton, LayerSpaces spaces, PoseItemTemplate template) throws MalformedKumoTemplateException
    {
        IPoseItem item = createPlainItem(context, skeleton, spaces, template);
        if (template.mirror || template.swapSides)
        {
            if (spaces.mirror == null)
            {
                throw new MalformedKumoTemplateException("An item sets \"mirror\" / \"swapSides\" but its layer has no \"mirror\" rule.");
            }
            item = new MirroredPoseItem(item, spaces.mirror, skeleton, template.mirror);
        }
        return item;
    }

    private static IPoseItem createPlainItem(IKumoInstancingContext context, Skeleton skeleton, LayerSpaces spaces, PoseItemTemplate template) throws MalformedKumoTemplateException
    {
        IPoseItem item = createUnconditionalItem(context, skeleton, spaces, template);
        // The item is skipped while its condition does not hold.
        if (template.when == null)
        {
            return item;
        }
        return new ConditionalPoseItem(item, Expression.compileCondition(template.when, context.getExpressionScope()));
    }

    private static IPoseItem createUnconditionalItem(IKumoInstancingContext context, Skeleton skeleton, LayerSpaces spaces, PoseItemTemplate template) throws MalformedKumoTemplateException
    {
        ItemEffects effects = new ItemEffects(context.getExpressionScope(), skeleton, template.damping, template.vectorModes, template.snap);

        if (template instanceof ClipItemTemplate)
        {
            ClipItemTemplate clipTemplate = (ClipItemTemplate) template;
            KeyframeAnimation animation = requireAnimation(context, clipTemplate.animationKey);
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
                clipValues.put("duration", Expression.constant(duration));
            }
            Expression frame = clipTemplate.frame == null ? null
                    : Expression.compile(clipTemplate.frame.json, context.getExpressionScope().withValues(clipValues));

            return new ClipPoseItem(binding, frame,
                    Expression.compile(clipTemplate.weight, context.getExpressionScope(), Expression.ONE),
                    template.space,
                    clipLength, duration, spaces, effects.isEmpty() ? null : effects);
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
    public float getElapsedTicks()
    {
        return elapsed;
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
    public boolean isAnimationFinished()
    {
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
        elapsed = 0;
        snapPending = snapSlots.length > 0;
        for (Expression expression : held)
        {
            expression.restart(context);
        }
        for (IPoseItem item : items)
        {
            item.onNodeStarted(context);
        }
        if (!enterItems.isEmpty())
        {
            if (enterPose == null || enterPose.size() != skeleton.size())
            {
                enterPose = new Pose(skeleton);
            }
            enterPose.clear();
            for (IPoseItem item : enterItems)
            {
                item.onNodeStarted(context);
                item.apply(enterPose, context, 0);
            }
            enterPending = true;
        }
    }

    @Override
    public void evaluate(IKumoContext context, Pose pose) throws MalformedKumoTemplateException
    {
        for (IPoseItem item : items)
        {
            item.apply(pose, context, elapsed);
        }

        // Node-level damping applies to whatever this node wrote and no item damped already.
        for (int i = 0; i < dampingSlots.length; i++)
        {
            if (dampingSlots[i] >= 0)
            {
                BoneTarget target = pose.get(dampingSlots[i]);
                if (Float.isNaN(target.smoothness) && isUndamped(target.vectorSmoothness))
                {
                    ItemEffects.applyDamping(target, dampingValues[i]);
                }
            }
            else
            {
                for (int slot = 0; slot < pose.size(); slot++)
                {
                    BoneTarget target = pose.get(slot);
                    if ((target.hasRotation || target.hasPre || target.hasPost || target.hasVector) && Float.isNaN(target.smoothness) && isUndamped(target.vectorSmoothness))
                    {
                        ItemEffects.applyDamping(target, dampingValues[i]);
                    }
                }
            }
        }

        if (enterPending)
        {
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
            enterPending = false;
        }

        if (snapPending)
        {
            for (int slot : snapSlots)
            {
                BoneTarget target = pose.get(slot);
                target.snap = true;
                target.vectorMode = IVectorSink.Mode.SNAP;
            }
            snapPending = false;
        }

        for (int slot = 0; slot < pose.size(); slot++)
        {
            BoneTarget target = pose.get(slot);
            if (target.hasVector && isUndamped(target.vectorSmoothness) && target.vectorMode == IVectorSink.Mode.RETARGET)
            {
                // Keyframed root motion is already smooth; without an explicit damping entry it is applied as is.
                target.vectorMode = IVectorSink.Mode.SNAP;
            }
        }
    }

    private static boolean isUndamped(Vec3f smoothness)
    {
        return Float.isNaN(smoothness.x) && Float.isNaN(smoothness.y) && Float.isNaN(smoothness.z);
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
        elapsed += deltaTime;
        for (IPoseItem item : items)
        {
            item.advance(context, deltaTime);
        }
    }

}
