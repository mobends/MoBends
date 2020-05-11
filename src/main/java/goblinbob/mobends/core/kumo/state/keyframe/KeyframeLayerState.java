package goblinbob.mobends.core.kumo.state.keyframe;

import goblinbob.mobends.core.animation.keyframe.Bone;
import goblinbob.mobends.core.animation.keyframe.Keyframe;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.*;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeLayerTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;
import goblinbob.mobends.core.util.KeyframeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyframeLayerState implements ILayerState
{

    private List<INodeState> nodeStates = new ArrayList<>();
    private INodeState previousNode;
    private INodeState currentNode;

    public KeyframeLayerState(IKumoInstancingContext context, KeyframeLayerTemplate layerTemplate) throws MalformedKumoTemplateException
    {
        for (KeyframeNodeTemplate nodeTemplate : layerTemplate.nodes)
        {
            nodeStates.add(KeyframeNodeRegistry.INSTANCE.createFromTemplate(context, nodeTemplate));
        }

        for (int i = 0; i < nodeStates.size(); ++i)
        {
            nodeStates.get(i).parseConnections(nodeStates, layerTemplate.nodes.get(i));
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
    public void start()
    {
    }

    @Override
    public void update(IKumoContext context, float deltaTime) throws MalformedKumoTemplateException
    {
        if (currentNode != null)
        {
            KeyframeAnimation animation = currentNode.getAnimation();

            if (animation != null)
            {
                applyKeyframeAnimation(context.getEntityData(), animation, currentNode.getProgress());
            }
        }

        // Updating node states.
        for (INodeState node : nodeStates)
        {
            node.update(deltaTime);
        }

        // Populating the context.
        context.setCurrentNode(currentNode);

        // Evaluating connection trigger conditions.
        for (ConnectionState connection : currentNode.getConnections())
        {
            if (connection.triggerCondition.isConditionMet(context))
            {
                currentNode = connection.targetNode;
                currentNode.start();
                break;
            }
        }
    }

    public void applyKeyframeAnimation(EntityData<?> entityData, KeyframeAnimation animation, float keyframeIndex)
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
                KeyframeUtils.tweenVector(entityData.globalOffset, keyframe.position, nextFrame.position, tween);
            }
        }

        if (shouldPartBeAffected("centerRotation") && animation.bones.containsKey("centerRotation"))
        {
            final Bone rootBone = animation.bones.get("centerRotation");
            final Keyframe keyframe = rootBone.keyframes.get(frameA);
            final Keyframe nextFrame = rootBone.keyframes.get(frameB);

            if (keyframe != null && nextFrame != null)
            {
                KeyframeUtils.tweenOrientation(entityData.centerRotation, keyframe.rotation, nextFrame.rotation, tween);
                KeyframeUtils.tweenVector(entityData.globalOffset, keyframe.position, nextFrame.position, tween);
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
                            KeyframeUtils.tweenOrientation(((IModelPart) part).getRotation(), keyframe.rotation, nextFrame.rotation, tween);
                        }
                    }
                }
            }
        }
    }

    private boolean shouldPartBeAffected(String partName)
    {
        return true;
    }

    public static KeyframeLayerState createFromTemplate(IKumoInstancingContext data, KeyframeLayerTemplate layerTemplate) throws MalformedKumoTemplateException
    {
        return new KeyframeLayerState(data, layerTemplate);
    }

}
