package goblinbob.mobends.core.kumo.state.keyframe;

import goblinbob.mobends.core.animation.keyframe.ArmatureMask;
import goblinbob.mobends.core.kumo.pose.BoneTarget;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.PoseMath;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.*;
import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.ConnectionTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeLayerTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;
import goblinbob.mobends.core.util.Tween;

import java.util.*;

/**
 * A state machine of pose nodes. Each frame the current node (and, during a transition, the
 * previous node or a frozen snapshot of the blend) is evaluated, the two are cross-faded, and the
 * result is composited onto the animator pose in the layer's mode.
 */
public class KeyframeLayerState implements ILayerState
{

    private final List<INodeState> nodeStates = new ArrayList<>();
    private final Map<String, INodeState> nodesByName = new HashMap<>();
    private final ArmatureMask mask;
    private final LayerTemplate.LayerMode mode;
    private final Skeleton skeleton;
    private final boolean[] allowed;
    private final ITriggerCondition when;
    private final VariableScope variables = new VariableScope();

    private final Pose currentPose;
    private final Pose previousPose;
    private final Pose snapshotPose;
    private final Pose outputPose;
    private final Pose lastOutput;
    private final goblinbob.mobends.core.math.Quaternion blendTemp = new goblinbob.mobends.core.math.Quaternion();

    private INodeState previousNode;
    private INodeState currentNode;
    private boolean previousIsSnapshot;
    private float transitionProgress = 0.0F;
    private float transitionDuration = 0.0F;
    private ConnectionTemplate.Easing transitionEasing = ConnectionTemplate.Easing.EASE_IN_OUT;
    private float elapsedTicks = 0.0F;

    public KeyframeLayerState(IKumoInstancingContext context, Skeleton skeleton, KeyframeLayerTemplate layerTemplate) throws MalformedKumoTemplateException
    {
        this.mask = layerTemplate.mask;
        this.mode = layerTemplate.mode == null ? LayerTemplate.LayerMode.OVERRIDE : layerTemplate.mode;
        this.skeleton = skeleton;
        this.when = layerTemplate.when == null ? null : TriggerConditionRegistry.instance.createFromTemplate(layerTemplate.when);
        this.variables.putAll(layerTemplate.variables);

        if (layerTemplate.nodes == null || layerTemplate.nodes.isEmpty())
        {
            throw new MalformedKumoTemplateException("A keyframe layer has no nodes.");
        }

        for (int i = 0; i < layerTemplate.nodes.size(); i++)
        {
            KeyframeNodeTemplate nodeTemplate = layerTemplate.nodes.get(i);
            if (nodeTemplate.name == null)
            {
                nodeTemplate.name = Integer.toString(i);
            }
            INodeState node = KeyframeNodeRegistry.INSTANCE.createFromTemplate(context, skeleton, layerTemplate, nodeTemplate);
            nodeStates.add(node);
            if (nodesByName.put(nodeTemplate.name, node) != null)
            {
                throw new MalformedKumoTemplateException(String.format("Two nodes share the name '%s'.", nodeTemplate.name));
            }
        }

        for (int i = 0; i < nodeStates.size(); i++)
        {
            nodeStates.get(i).parseConnections(nodeStates, nodesByName, layerTemplate.nodes.get(i));
        }

        if (layerTemplate.entryNodeName != null)
        {
            currentNode = nodesByName.get(layerTemplate.entryNodeName);
            if (currentNode == null)
            {
                throw new MalformedKumoTemplateException(String.format("Entry node '%s' doesn't exist.", layerTemplate.entryNodeName));
            }
        }
        else
        {
            if (layerTemplate.entryNode < 0 || layerTemplate.entryNode >= nodeStates.size())
            {
                throw new MalformedKumoTemplateException("Entry node index is out of bounds.");
            }
            currentNode = nodeStates.get(layerTemplate.entryNode);
        }

        // Poses are sized after every node has registered its bones.
        currentPose = new Pose(skeleton);
        previousPose = new Pose(skeleton);
        snapshotPose = new Pose(skeleton);
        outputPose = new Pose(skeleton);
        lastOutput = new Pose(skeleton);

        allowed = new boolean[skeleton.size()];
        for (int i = 0; i < skeleton.size(); i++)
        {
            allowed[i] = mask == null || mask.doesAllow(skeleton.nameOf(i));
        }
    }

    @Override
    public void start(IKumoContext context)
    {
        bindScopes(context, currentNode);
        currentNode.start(context);
    }

    private void bindScopes(IKumoContext context, INodeState node)
    {
        context.setCurrentNode(node);
        if (context instanceof KumoContext)
        {
            ((KumoContext) context).layerScope = variables;
            ((KumoContext) context).nodeScope = node == null ? null : node.getScope();
        }
    }

    @Override
    public float getElapsedTicks()
    {
        return elapsedTicks;
    }

    @Override
    public Collection<String> getActions()
    {
        return currentNode.getTags();
    }

    public INodeState getCurrentNode()
    {
        return currentNode;
    }

    @Override
    public void update(IKumoContext context, float deltaTime, Pose animatorPose) throws MalformedKumoTemplateException
    {
        bindScopes(context, currentNode);

        if (when != null && !when.isConditionMet(context))
        {
            // Disabled: the layer writes nothing and its clocks pause.
            return;
        }

        // 1. Transitions are decided before posing, so a state change shows up on the frame it happens.
        //    Every condition is evaluated each frame (edge triggers such as core:decreased track the
        //    variable they watch); the first one met wins.
        ConnectionState fired = null;
        for (ConnectionState connection : currentNode.getConnections())
        {
            if (connection.triggerCondition.isConditionMet(context) && fired == null)
            {
                fired = connection;
            }
        }
        if (fired != null)
        {
            beginTransition(fired, context);
        }

        // 2. Evaluate.
        bindScopes(context, currentNode);
        currentPose.clear();
        currentNode.evaluate(context, currentPose);

        Pose result = currentPose;
        if (previousNode != null)
        {
            float t = ease(transitionDuration <= 0 ? 1F : transitionProgress / transitionDuration);
            Pose source;
            if (previousIsSnapshot)
            {
                source = snapshotPose;
            }
            else
            {
                bindScopes(context, previousNode);
                previousPose.clear();
                previousNode.evaluate(context, previousPose);
                source = previousPose;
                bindScopes(context, currentNode);
            }
            blend(source, currentPose, t, outputPose);
            result = outputPose;
        }

        // 3. Composite onto the animator pose.
        composite(result, animatorPose);
        lastOutput.set(result);

        // 4. Advance clocks.
        elapsedTicks += deltaTime;
        currentNode.advance(context, deltaTime);
        if (previousNode != null)
        {
            if (!previousIsSnapshot)
            {
                previousNode.advance(context, deltaTime);
            }
            transitionProgress += deltaTime;
            if (transitionProgress >= transitionDuration)
            {
                previousNode = null;
                previousIsSnapshot = false;
            }
        }
    }

    private void beginTransition(ConnectionState connection, IKumoContext context)
    {
        transitionDuration = connection.transitionDuration;
        transitionEasing = connection.transitionEasing;

        if (transitionDuration <= 0.0F || connection.targetNode == currentNode)
        {
            previousNode = null;
            previousIsSnapshot = false;
        }
        else if (previousNode != null)
        {
            // Interrupting a transition: freeze what is on screen and fade from that (no pop).
            snapshotPose.set(lastOutput);
            previousNode = currentNode;
            previousIsSnapshot = true;
            transitionProgress = 0;
        }
        else
        {
            previousNode = currentNode;
            previousIsSnapshot = false;
            transitionProgress = 0;
        }

        if (connection.set != null && context.getLayerScope() != null)
        {
            for (java.util.Map.Entry<String, Float> entry : connection.set.entrySet())
            {
                context.getLayerScope().set(entry.getKey(), entry.getValue());
            }
        }

        currentNode = connection.targetNode;
        bindScopes(context, currentNode);
        currentNode.start(context);
    }

    private float ease(float t)
    {
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        switch (transitionEasing)
        {
            case EASE_IN:
                return (float) Tween.easeIn(t, 2.0);
            case EASE_OUT:
                return (float) Tween.easeOut(t, 2.0);
            case EASE_IN_OUT:
                return (float) Tween.easeInOut(t, 2.0);
            case EXPONENTIAL:
                return (float) ((1.0 - Math.exp(-4.0 * t)) / (1.0 - Math.exp(-4.0)));
            case LINEAR:
            default:
                return t;
        }
    }

    /** dest = nlerp(from, to, t); bones present on one side only are taken from that side. */
    private void blend(Pose from, Pose to, float t, Pose dest)
    {
        dest.clear();
        for (int i = 0; i < dest.size(); i++)
        {
            BoneTarget a = from.get(i);
            BoneTarget b = to.get(i);
            BoneTarget d = dest.get(i);

            blendQuat(a.hasRotation, a.rotation, b.hasRotation, b.rotation, t, d.rotation);
            d.hasRotation = a.hasRotation || b.hasRotation;
            blendQuat(a.hasPre, a.pre, b.hasPre, b.pre, t, d.pre);
            d.hasPre = !d.hasRotation && (a.hasPre || b.hasPre);
            blendQuat(a.hasPost, a.post, b.hasPost, b.post, t, d.post);
            d.hasPost = !d.hasRotation && (a.hasPost || b.hasPost);

            if (a.hasOffset && b.hasOffset)
            {
                d.offset.set(a.offset.x + (b.offset.x - a.offset.x) * t, a.offset.y + (b.offset.y - a.offset.y) * t, a.offset.z + (b.offset.z - a.offset.z) * t);
                d.hasOffset = true;
            }
            else if (a.hasOffset || b.hasOffset)
            {
                d.offset.set(a.hasOffset ? a.offset : b.offset);
                d.hasOffset = true;
            }
            d.offsetAdditive = b.hasOffset ? b.offsetAdditive : a.offsetAdditive;

            if (a.hasVector && b.hasVector)
            {
                d.vector.set(a.vector.x + (b.vector.x - a.vector.x) * t, a.vector.y + (b.vector.y - a.vector.y) * t, a.vector.z + (b.vector.z - a.vector.z) * t);
                d.hasVector = true;
            }
            else if (a.hasVector || b.hasVector)
            {
                d.vector.set(a.hasVector ? a.vector : b.vector);
                d.hasVector = true;
            }
            d.vectorAdditive = b.hasVector ? b.vectorAdditive : a.vectorAdditive;

            // Damping, snapping and vector modes follow the node being entered.
            d.smoothness = b.smoothness;
            d.vectorSmoothness.set(b.vectorSmoothness);
            d.snap = b.snap;
            d.vectorMode = b.vectorMode;
            d.hasSnapFrom = b.hasSnapFrom;
            d.snapFrom.set(b.snapFrom);
            d.hasVectorStart = b.hasVectorStart;
            d.vectorStart.set(b.vectorStart);
        }
    }

    private void blendQuat(boolean hasA, goblinbob.mobends.core.math.Quaternion a, boolean hasB, goblinbob.mobends.core.math.Quaternion b, float t, goblinbob.mobends.core.math.Quaternion dest)
    {
        if (hasA && hasB)
        {
            PoseMath.nlerp(a, b, t, dest);
        }
        else if (hasA)
        {
            dest.set(a);
        }
        else if (hasB)
        {
            dest.set(b);
        }
    }

    private void composite(Pose layerPose, Pose animatorPose)
    {
        for (int i = 0; i < layerPose.size(); i++)
        {
            if (!allowed[i])
            {
                continue;
            }
            BoneTarget src = layerPose.get(i);
            BoneTarget dst = animatorPose.get(i);
            boolean any = src.hasRotation || src.hasPre || src.hasPost || src.hasOffset || src.hasVector;
            if (!any)
            {
                continue;
            }

            if (src.hasRotation || src.hasPre || src.hasPost)
            {
                animatorPose.composeRotation(i, src);
            }
            if (src.hasOffset)
            {
                animatorPose.composeOffset(i, src.offset.x, src.offset.y, src.offset.z, src.offsetAdditive ? Pose.Space.PRE : Pose.Space.OVERRIDE);
            }
            if (src.hasVector)
            {
                animatorPose.composeVector(i, src.vector.x, src.vector.y, src.vector.z, src.vectorAdditive ? Pose.Space.PRE : Pose.Space.OVERRIDE);
            }
            if (src.hasSnapFrom)
            {
                dst.hasSnapFrom = true;
                dst.snapFrom.set(src.snapFrom);
            }
            if (src.hasVectorStart)
            {
                dst.hasVectorStart = true;
                dst.vectorStart.set(src.vectorStart);
            }

            if (!Float.isNaN(src.smoothness)) dst.smoothness = src.smoothness;
            if (!Float.isNaN(src.vectorSmoothness.x)) dst.vectorSmoothness.x = src.vectorSmoothness.x;
            if (!Float.isNaN(src.vectorSmoothness.y)) dst.vectorSmoothness.y = src.vectorSmoothness.y;
            if (!Float.isNaN(src.vectorSmoothness.z)) dst.vectorSmoothness.z = src.vectorSmoothness.z;
            if (src.snap) dst.snap = true;
            if (src.restartX) dst.restartX = true;
            if (src.restartY) dst.restartY = true;
            if (src.restartZ) dst.restartZ = true;
            if (src.hasVector) dst.vectorMode = src.vectorMode;
        }
    }

    public static KeyframeLayerState createFromTemplate(IKumoInstancingContext data, Skeleton skeleton, KeyframeLayerTemplate layerTemplate) throws MalformedKumoTemplateException
    {
        return new KeyframeLayerState(data, skeleton, layerTemplate);
    }

}
