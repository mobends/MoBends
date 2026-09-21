package goblinbob.mobends.core.kumo.state.keyframe;

import goblinbob.mobends.core.animation.keyframe.ArmatureMask;
import goblinbob.mobends.core.kumo.pose.BoneTarget;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.PoseMath;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.*;
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
    private final Pose.Space[] additiveSpaces;
    private final Skeleton skeleton;
    private final boolean[] allowed;

    private final Pose currentPose;
    private final Pose previousPose;
    private final Pose snapshotPose;
    private final Pose outputPose;
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

        additiveSpaces = new Pose.Space[skeleton.size()];
        allowed = new boolean[skeleton.size()];
        Pose.Space defaultSpace = layerTemplate.defaultAdditiveSpace();
        for (int i = 0; i < skeleton.size(); i++)
        {
            String bone = skeleton.nameOf(i);
            additiveSpaces[i] = layerTemplate.additiveSpace == null ? defaultSpace : layerTemplate.additiveSpace.forBone(bone, defaultSpace);
            allowed[i] = mask == null || mask.doesAllow(bone);
        }
    }

    @Override
    public void start(IKumoContext context)
    {
        context.setCurrentNode(currentNode);
        currentNode.start(context);
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
        context.setCurrentNode(currentNode);

        // 1. Evaluate.
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
                previousPose.clear();
                previousNode.evaluate(context, previousPose);
                source = previousPose;
            }
            blend(source, currentPose, t, outputPose);
            result = outputPose;
        }

        // 2. Composite onto the animator pose.
        composite(result, animatorPose);

        // 3. Advance clocks.
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

        // 4. Evaluate connections.
        for (ConnectionState connection : currentNode.getConnections())
        {
            if (connection.triggerCondition.isConditionMet(context))
            {
                beginTransition(connection, result, context);
                break;
            }
        }
    }

    private void beginTransition(ConnectionState connection, Pose currentOutput, IKumoContext context)
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
            snapshotPose.set(currentOutput);
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

        currentNode = connection.targetNode;
        context.setCurrentNode(currentNode);
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

            if (a.hasRotation && b.hasRotation)
            {
                PoseMath.nlerp(a.rotation, b.rotation, t, d.rotation);
                d.hasRotation = true;
            }
            else if (a.hasRotation || b.hasRotation)
            {
                d.rotation.set(a.hasRotation ? a.rotation : b.rotation);
                d.hasRotation = true;
            }

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

            // Damping, snapping and vector modes follow the node being entered.
            d.smoothness = b.smoothness;
            d.vectorSmoothness.set(b.vectorSmoothness);
            d.snap = b.snap;
            d.vectorMode = b.vectorMode;
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
            if (!src.hasRotation && !src.hasOffset && !src.hasVector)
            {
                continue;
            }

            Pose.Space space = mode == LayerTemplate.LayerMode.ADDITIVE ? additiveSpaces[i] : Pose.Space.OVERRIDE;

            if (src.hasRotation)
            {
                animatorPose.composeRotation(i, src.rotation, space);
            }
            if (src.hasOffset)
            {
                animatorPose.composeOffset(i, src.offset.x, src.offset.y, src.offset.z, space);
            }
            if (src.hasVector)
            {
                animatorPose.composeVector(i, src.vector.x, src.vector.y, src.vector.z, space);
            }

            if (!Float.isNaN(src.smoothness)) dst.smoothness = src.smoothness;
            if (!Float.isNaN(src.vectorSmoothness.x)) dst.vectorSmoothness.x = src.vectorSmoothness.x;
            if (!Float.isNaN(src.vectorSmoothness.y)) dst.vectorSmoothness.y = src.vectorSmoothness.y;
            if (!Float.isNaN(src.vectorSmoothness.z)) dst.vectorSmoothness.z = src.vectorSmoothness.z;
            if (src.snap) dst.snap = true;
            if (src.hasVector) dst.vectorMode = src.vectorMode;
        }
    }

    public static KeyframeLayerState createFromTemplate(IKumoInstancingContext data, Skeleton skeleton, KeyframeLayerTemplate layerTemplate) throws MalformedKumoTemplateException
    {
        return new KeyframeLayerState(data, skeleton, layerTemplate);
    }

}
