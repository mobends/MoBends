package goblinbob.mobends.core.kumo.state.keyframe;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.bind.IVectorSink;
import goblinbob.mobends.core.kumo.driver.DriverRegistry;
import goblinbob.mobends.core.kumo.pose.*;
import goblinbob.mobends.core.kumo.state.ConnectionState;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.VariableScope;
import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.DampingTemplate;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.*;
import goblinbob.mobends.core.kumo.state.template.pose.ClipItemTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.PoseItemTemplate;

import java.util.*;

/**
 * A node of a keyframe layer: an ordered stack of pose items (clips, drivers), a damping table,
 * a list of bones to snap on entry, and the outgoing connections. The legacy
 * {@code core:standard} and {@code core:movement} nodes are pose nodes with a single legacy clip.
 */
public class PoseNode implements INodeState
{

    private final String name;
    private final List<String> tags;
    private final List<IPoseItem> items;
    private final IPoseItem primary;
    private final int[] dampingSlots;
    private final float[][] dampingValues;
    private final int[] snapSlots;
    private final List<ConnectionState> connections = new ArrayList<>();
    private final List<IPoseItem> enterItems;
    private final Skeleton skeleton;
    private final VariableScope scope = new VariableScope();
    private Map<String, Float> setOnEnter;
    private Pose enterPose;
    private boolean enterPending;

    /**
     * Legacy nodes (core:standard / core:movement) hard-set every bone they touch, as the original
     * KUMO did; format 2 nodes hand their targets to the bones' own smoothing.
     */
    private boolean hardSet;

    private float elapsed;
    private boolean snapPending;

    public PoseNode(String name, List<String> tags, List<IPoseItem> items, List<IPoseItem> enterItems, Skeleton skeleton, DampingTemplate layerDamping, DampingTemplate nodeDamping, List<String> snapOnEnter)
    {
        this.name = name;
        this.tags = tags == null ? Collections.<String>emptyList() : tags;
        this.items = items;
        this.enterItems = enterItems == null ? Collections.<IPoseItem>emptyList() : enterItems;
        this.skeleton = skeleton;
        this.primary = items.isEmpty() ? null : items.get(0);

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

    public static PoseNode createStandard(IKumoInstancingContext context, Skeleton skeleton, LayerTemplate layer, StandardKeyframeNodeTemplate template) throws MalformedKumoTemplateException
    {
        List<IPoseItem> items = new ArrayList<>();
        if (template.animationKey != null)
        {
            KeyframeAnimation animation = requireAnimation(context, template.animationKey);
            items.add(new LegacyClipPoseItem(new ClipBinding(animation, skeleton, null), template.startFrame, template.playbackSpeed, template.looping, false));
        }
        PoseNode node = new PoseNode(template.name, template.tags, items, null, skeleton, layer.damping, template.damping, template.snapOnEnter);
        node.hardSet = true;
        return node;
    }

    public static PoseNode createMovement(IKumoInstancingContext context, Skeleton skeleton, LayerTemplate layer, MovementKeyframeNodeTemplate template) throws MalformedKumoTemplateException
    {
        List<IPoseItem> items = new ArrayList<>();
        if (template.animationKey != null)
        {
            KeyframeAnimation animation = requireAnimation(context, template.animationKey);
            items.add(new LegacyClipPoseItem(new ClipBinding(animation, skeleton, null), template.startFrame, template.playbackSpeed, false, true));
        }
        PoseNode node = new PoseNode(template.name, template.tags, items, null, skeleton, layer.damping, template.damping, template.snapOnEnter);
        node.hardSet = true;
        return node;
    }

    public static PoseNode createPose(IKumoInstancingContext context, Skeleton skeleton, LayerTemplate layer, PoseNodeTemplate template) throws MalformedKumoTemplateException
    {
        LayerSpaces spaces = new LayerSpaces(skeleton, layer);
        List<IPoseItem> items = new ArrayList<>();
        if (template.pose != null)
        {
            for (PoseItemTemplate itemTemplate : template.pose)
            {
                items.add(createItem(context, skeleton, spaces, itemTemplate));
            }
        }
        List<IPoseItem> enterItems = new ArrayList<>();
        if (template.enterPose != null)
        {
            for (PoseItemTemplate itemTemplate : template.enterPose)
            {
                enterItems.add(createItem(context, skeleton, spaces, itemTemplate));
            }
        }
        PoseNode node = new PoseNode(template.name, template.tags, items, enterItems, skeleton, layer.damping, template.damping, template.snapOnEnter);
        node.setOnEnter = template.set;
        return node;
    }

    private static IPoseItem createItem(IKumoInstancingContext context, Skeleton skeleton, LayerSpaces spaces, PoseItemTemplate template) throws MalformedKumoTemplateException
    {
        ITriggerCondition when = template.when == null ? null : TriggerConditionRegistry.instance.createFromTemplate(template.when);
        ItemEffects effects = new ItemEffects(skeleton, template.damping, template.vectorModes, template.snap);

        if (template instanceof ClipItemTemplate)
        {
            ClipItemTemplate clipTemplate = (ClipItemTemplate) template;
            KeyframeAnimation animation = requireAnimation(context, clipTemplate.animationKey);
            ClipBinding binding = new ClipBinding(animation, skeleton, clipTemplate.bones);

            float duration = clipTemplate.duration != null ? clipTemplate.duration
                    : animation.duration != null ? animation.duration
                    : Math.max(binding.keyframeCount - 1, 0);
            boolean loop = clipTemplate.loop != null ? clipTemplate.loop
                    : animation.loop != null ? animation.loop : false;

            return new ClipPoseItem(binding,
                    TimeSource.fromTemplate(clipTemplate.time),
                    ValueSource.fromTemplate(clipTemplate.weight, ValueSource.ONE),
                    template.space,
                    when, duration, loop, spaces, effects.isEmpty() ? null : effects);
        }

        if (template instanceof DriverItemTemplate)
        {
            IPoseItem driver = DriverRegistry.INSTANCE.create(context, skeleton, (DriverItemTemplate) template);
            // A ramp's "when" is its own up/down switch; every other driver is skipped while its
            // condition does not hold, like a clip.
            boolean ownsCondition = "core:ramp".equals(((DriverItemTemplate) template).driver);
            return when == null || ownsCondition ? driver : new goblinbob.mobends.core.kumo.pose.ConditionalPoseItem(driver, when);
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
    public Collection<String> getTags()
    {
        return tags;
    }

    @Override
    public Iterable<ConnectionState> getConnections()
    {
        return connections;
    }

    @Override
    public float getElapsedTicks()
    {
        return elapsed;
    }

    @Override
    public boolean isAnimationFinished()
    {
        return primary == null || primary.isFinished(elapsed);
    }

    @Override
    public void parseConnections(List<INodeState> nodeStates, Map<String, INodeState> nodesByName, KeyframeNodeTemplate template) throws MalformedKumoTemplateException
    {
        if (template.connections != null)
        {
            for (ConnectionTemplate connectionTemplate : template.connections)
            {
                connections.add(ConnectionState.createFromTemplate(nodeStates, nodesByName, connectionTemplate));
            }
        }
    }

    @Override
    public VariableScope getScope()
    {
        return scope;
    }

    @Override
    public void start(IKumoContext context)
    {
        elapsed = 0;
        snapPending = snapSlots.length > 0;
        if (setOnEnter != null && context.getLayerScope() != null)
        {
            for (Map.Entry<String, Float> entry : setOnEnter.entrySet())
            {
                context.getLayerScope().set(entry.getKey(), entry.getValue());
            }
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
            try
            {
                for (IPoseItem item : enterItems)
                {
                    item.onNodeStarted(context);
                    item.apply(enterPose, context, 0);
                }
                enterPending = true;
            }
            catch (MalformedKumoTemplateException e)
            {
                throw new IllegalStateException(e);
            }
        }
        for (ConnectionState connection : connections)
        {
            connection.triggerCondition.onNodeStarted(context);
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
            if (hardSet)
            {
                if (target.hasRotation || target.hasPre || target.hasPost) target.snap = true;
                if (target.hasVector) target.vectorMode = IVectorSink.Mode.SNAP;
            }
            else if (target.hasVector && isUndamped(target.vectorSmoothness) && target.vectorMode == IVectorSink.Mode.RETARGET)
            {
                // Keyframed root motion is already smooth; without an explicit damping entry it is applied as is.
                target.vectorMode = IVectorSink.Mode.SNAP;
            }
        }
    }

    private static boolean isUndamped(goblinbob.mobends.core.math.vector.Vec3f smoothness)
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
