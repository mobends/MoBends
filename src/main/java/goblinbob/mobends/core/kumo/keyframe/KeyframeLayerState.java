package goblinbob.mobends.core.kumo.keyframe;

import goblinbob.mobends.core.animation.keyframe.ArmatureMask;
import goblinbob.mobends.core.animation.keyframe.Bone;
import goblinbob.mobends.core.animation.keyframe.Keyframe;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.*;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.util.KeyframeUtils;
import goblinbob.mobends.core.util.Tween;
import goblinbob.mobends.forge.IModelPart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyframeLayerState<D extends IEntityData> implements ILayerState<D>
{
    private List<INodeState<D>> nodeStates = new ArrayList<>();
    private ArmatureMask mask;
    private INodeState<D> previousNode;
    private INodeState<D> currentNode;
    private float transitionProgress = 0.0F;
    private float transitionDuration = 0.0F;
    private ConnectionTemplate.Easing transitionEasing = ConnectionTemplate.Easing.EASE_IN_OUT;

    public KeyframeLayerState(KeyframeLayerTemplate layerTemplate, IKumoInstancingContext<D> context) throws MalformedKumoTemplateException
    {
        this.mask = layerTemplate.mask;

        for (KeyframeNodeTemplate nodeTemplate : layerTemplate.nodes)
        {
            nodeStates.add(context.createNodeState(context, nodeTemplate));
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
            throw new MalformedKumoTemplateException("Entry node index is out of bounds.");
        }
    }

    @Override
    public void start(IKumoContext<D> context)
    {
        currentNode.start(context);
    }

    @Override
    public void update(IKumoContext<D> context, float deltaTime) throws MalformedKumoTemplateException
    {
        final IEntityData data = context.getEntityData();

        if (currentNode != null)
        {
            KeyframeAnimation animation = currentNode.getAnimation();

            if (animation != null)
            {
                applyRestPose(data, animation);

                if (previousNode != null)
                {
                    float t = transitionProgress / transitionDuration;
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
                    applyKeyframeAnimation(data, previousAnimation, previousNode.getProgress(), 1 - t);
                    applyKeyframeAnimation(data, animation, currentNode.getProgress(), t);

                    transitionProgress += deltaTime;
                    if (transitionProgress >= transitionDuration)
                    {
                        previousNode = null;
                    }
                }
                else
                {
                    applyKeyframeAnimation(data, animation, currentNode.getProgress(), 1.0F);
                }
            }
        }

        // Populating the context.
        context.setCurrentNode(currentNode);

        // Updating node states.
        for (INodeState<D> node : nodeStates)
        {
            node.update(context, deltaTime);
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
                    transitionProgress = 0;
                }

                currentNode = connection.targetNode;
                currentNode.start(context);

                break;
            }
        }
    }

    public void applyRestPose(IEntityData entityData, KeyframeAnimation animation)
    {
        if (shouldPartBeAffected("root") && animation.bones.containsKey("root"))
        {
            entityData.globalOffset.set(0, 0, 0);
        }

        if ((shouldPartBeAffected("root") && animation.bones.containsKey("root")) ||
            (shouldPartBeAffected("centerRotation") && animation.bones.containsKey("centerRotation")))
        {
            entityData.centerRotation.set(0F, 0F, 0F, 0F);
        }

        for (Map.Entry<String, Bone> entry : animation.bones.entrySet())
        {
            final String key = entry.getKey();

            if (shouldPartBeAffected(key))
            {
                Object part = entityData.getPartForName(key);

                if (part instanceof IModelPart)
                {
                    ((IModelPart) part).getRotation().set(0F, 0F, 0F, 0F);
                    ((IModelPart) part).getOffset().set(0F, 0F, 0F);
                }
            }
        }
    }

    public void applyKeyframeAnimation(IEntityData entityData, KeyframeAnimation animation, float keyframeIndex, float amount)
    {
        final int frameA = (int) keyframeIndex;
        final int frameB = (int) keyframeIndex + 1;
        final float tween = keyframeIndex - frameA;

        if (shouldPartBeAffected("root") && animation.bones.containsKey("root"))
        {
            final Bone rootBone = animation.bones.get("root");
            final Keyframe keyframe = rootBone.keyframes.get(frameA);
            final Keyframe nextFrame = rootBone.keyframes.get(frameB);

            if (keyframe != null && nextFrame != null)
            {
                KeyframeUtils.tweenVectorAdditive(entityData.globalOffset, keyframe.position, nextFrame.position, tween, amount);
            }
        }

        if (shouldPartBeAffected("centerRotation") && animation.bones.containsKey("centerRotation"))
        {
            final Bone rootBone = animation.bones.get("centerRotation");
            final Keyframe keyframe = rootBone.keyframes.get(frameA);
            final Keyframe nextFrame = rootBone.keyframes.get(frameB);

            if (keyframe != null && nextFrame != null)
            {
                KeyframeUtils.tweenOrientationAdditive(entityData.centerRotation, keyframe.rotation, nextFrame.rotation, tween, amount);
                KeyframeUtils.tweenVectorAdditive(entityData.globalOffset, keyframe.position, nextFrame.position, tween, amount);
            }
        }

        for (Map.Entry<String, Bone> entry : animation.bones.entrySet())
        {
            final String key = entry.getKey();

            if (shouldPartBeAffected(key))
            {
                Bone bone = entry.getValue();
                Object part = entityData.getPartForName(key);

                if (part != null)
                {
                    Keyframe keyframe = bone.keyframes.get(frameA);
                    Keyframe nextFrame = bone.keyframes.get(frameB);

                    if (keyframe != null && nextFrame != null)
                    {
                        if (part instanceof IModelPart)
                        {
                            KeyframeUtils.tweenOrientationAdditive(((IModelPart) part).getRotation(), keyframe.rotation, nextFrame.rotation, tween, amount);
                            // Note that the amount is negated.
                            KeyframeUtils.tweenVectorAdditive(((IModelPart) part).getOffset(), keyframe.position, nextFrame.position, tween, -amount);
                        }
                    }
                }
            }
        }
    }

    private boolean shouldPartBeAffected(String partName)
    {
        return mask == null || mask.doesAllow(partName);
    }

    public static <D extends IEntityData> KeyframeLayerState<D> createFromTemplate(IKumoInstancingContext<D> context, KeyframeLayerTemplate layerTemplate) throws MalformedKumoTemplateException
    {
        return new KeyframeLayerState<D>(layerTemplate, context);
    }
}
