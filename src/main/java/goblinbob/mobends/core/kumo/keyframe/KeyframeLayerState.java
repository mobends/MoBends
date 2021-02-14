package goblinbob.mobends.core.kumo.keyframe;

import goblinbob.mobends.core.IModelPart;
import goblinbob.mobends.core.animation.keyframe.ArmatureMask;
import goblinbob.mobends.core.animation.keyframe.Bone;
import goblinbob.mobends.core.animation.keyframe.Keyframe;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.AnimationRuntimeException;
import goblinbob.mobends.core.kumo.*;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.util.Tween;
import goblinbob.mobends.forge.KeyframeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyframeLayerState<D extends IEntityData> implements ILayerState<D>
{
    private List<INodeState<D>> nodeStates = new ArrayList<>();
    private ArmatureMask mask;
    private INodeState<D> previousNode;
    private INodeState<D> currentNode;
    private float transitionStartTime = 0.0F;
    private float transitionDuration = 0.0F;
    private ConnectionTemplate.Easing transitionEasing = ConnectionTemplate.Easing.EASE_IN_OUT;

    public KeyframeLayerState(KeyframeLayerTemplate layerTemplate, IKumoInstancingContext<D> context)
    {
        this.mask = layerTemplate.mask;

        for (KeyframeNodeTemplate nodeTemplate : layerTemplate.nodes)
        {
            nodeStates.add(nodeTemplate.instantiate(context));
        }

        for (int i = 0; i < nodeStates.size(); ++i)
        {
            nodeStates.get(i).parseConnections(nodeStates, layerTemplate.nodes[i], context);
        }

        try
        {
            currentNode = nodeStates.get(layerTemplate.entryNode);
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new AnimationRuntimeException("Entry node index is out of bounds.");
        }
    }

    @Override
    public void start(IKumoContext<D> context)
    {
        currentNode.start(context);
    }

    @Override
    public void update(IKumoContext<D> context)
    {
        final D data = context.getEntityData();

        if (currentNode != null)
        {
            KeyframeAnimation animation = currentNode.getAnimation();

            if (animation != null)
            {
                applyRestPose(data, animation);

                if (previousNode != null)
                {
                    float t = Math.min((context.getTicksPassed() - transitionStartTime) / transitionDuration, 1);
                    switch (transitionEasing)
                    {
                        case EASE_IN:
                            t = (float) Tween.easeIn(t, 2.0);
                            break;
                        case EASE_OUT:
                            t = (float) Tween.easeOut(t, 2.0);
                            break;
                        case EASE_IN_OUT:
                            t = (float) Tween.easeInOut(t, 2.0);
                            break;
                        case LINEAR:
                            break;
                    }

                    // Transition is in progress
                    KeyframeAnimation previousAnimation = previousNode.getAnimation();
                    applyKeyframeAnimation(data, previousAnimation, previousNode.getProgress(context), 1 - t);
                    applyKeyframeAnimation(data, animation, currentNode.getProgress(context), t);

                    if (t >= 1)
                    {
                        previousNode = null;
                    }
                }
                else
                {
                    applyKeyframeAnimation(data, animation, currentNode.getProgress(context), 1.0F);
                }
            }
        }

        // Populating the context.
        context.setCurrentNode(currentNode);

        // Updating node states.
        for (INodeState<D> node : nodeStates)
        {
            node.update(context);
        }

        // Evaluating connection trigger conditions.
        for (ConnectionState<D> connection : currentNode.getConnections())
        {
            if (connection.triggerCondition.isConditionMet(context))
            {
                // Transition setup
                transitionDuration = connection.transitionDuration;
                transitionEasing = connection.transitionEasing;
                if (transitionDuration == 0.0F)
                {
                    previousNode = null;
                }
                else
                {
                    previousNode = currentNode;
                    transitionStartTime = context.getTicksPassed();
                }

                currentNode = connection.targetNode;
                currentNode.start(context);

                break;
            }
        }
    }

    public void applyRestPose(D entityData, KeyframeAnimation animation)
    {
        IModelPart rootPart = entityData.getRootPart();

        if (shouldPartBeAffected("root") && animation.bones.containsKey("root"))
        {
            rootPart.getPosition().set(0, 0, 0);
        }

        if ((shouldPartBeAffected("root") && animation.bones.containsKey("root")) ||
            (shouldPartBeAffected("centerRotation") && animation.bones.containsKey("centerRotation")))
        {
            rootPart.getRotation().set(0, 0, 0, 0);
        }

        for (Map.Entry<String, Bone> entry : animation.bones.entrySet())
        {
            final String key = entry.getKey();

            if (shouldPartBeAffected(key))
            {
                IModelPart part = entityData.getPartForName(key);
                part.getRotation().set(0F, 0F, 0F, 0F);
                part.getOffset().set(0F, 0F, 0F);
            }
        }
    }

    public void applyKeyframeAnimation(IEntityData entityData, KeyframeAnimation animation, float keyframeIndex, float amount)
    {
        final int frameA = (int) keyframeIndex;
        final int frameB = (int) keyframeIndex + 1;
        final float tween = keyframeIndex - frameA;

        IModelPart rootPart = entityData.getRootPart();

        if (shouldPartBeAffected("root") && animation.bones.containsKey("root"))
        {
            final Bone rootBone = animation.bones.get("root");
            Keyframe[] keyframes = rootBone.getKeyframes();
            final Keyframe keyframe = keyframes[frameA];
            final Keyframe nextFrame = keyframes[frameB];

            if (keyframe != null && nextFrame != null)
            {
                KeyframeUtils.tweenVectorAdditive(rootPart.getPosition(), keyframe.position, nextFrame.position, tween, amount);
            }
        }

        if (shouldPartBeAffected("centerRotation") && animation.bones.containsKey("centerRotation"))
        {
            final Bone rootBone = animation.bones.get("centerRotation");
            Keyframe[] keyframes = rootBone.getKeyframes();
            final Keyframe keyframe = keyframes[frameA];
            final Keyframe nextFrame = keyframes[frameB];

            if (keyframe != null && nextFrame != null)
            {
                KeyframeUtils.tweenOrientationAdditive(rootPart.getRotation(), keyframe.rotation, nextFrame.rotation, tween, amount);
                KeyframeUtils.tweenVectorAdditive(rootPart.getPosition(), keyframe.position, nextFrame.position, tween, amount);
            }
        }

        for (Map.Entry<String, Bone> entry : animation.bones.entrySet())
        {
            final String key = entry.getKey();

            if (shouldPartBeAffected(key))
            {
                Bone bone = entry.getValue();
                Keyframe[] keyframes = bone.getKeyframes();
                IModelPart part = entityData.getPartForName(key);

                if (part != null)
                {
                    Keyframe keyframe = keyframes[frameA];
                    Keyframe nextFrame = keyframes[frameB];

                    if (keyframe != null && nextFrame != null)
                    {
                        KeyframeUtils.tweenOrientationAdditive(part.getRotation(), keyframe.rotation, nextFrame.rotation, tween, amount);
                        // Note that the amount is negated.
                        KeyframeUtils.tweenVectorAdditive(part.getOffset(), keyframe.position, nextFrame.position, tween, -amount);
                    }
                }
            }
        }
    }

    private boolean shouldPartBeAffected(String partName)
    {
        return mask == null || mask.doesAllow(partName);
    }
}
