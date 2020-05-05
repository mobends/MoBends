package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.animation.keyframe.Bone;
import goblinbob.mobends.core.animation.keyframe.Keyframe;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.*;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.NodeTemplate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KumoAnimator<D extends EntityData<?>>
{

    private final List<INodeState> nodeStates = new ArrayList<>();
    private INodeState currentNode;

    public KumoAnimator(AnimatorTemplate animatorTemplate, IKumoDataProvider dataProvider) throws MalformedKumoTemplateException
    {
        if (animatorTemplate.nodes == null)
        {
            throw new MalformedKumoTemplateException("No nodes were specified");
        }

        for (NodeTemplate template : animatorTemplate.nodes)
        {
            nodeStates.add(new NodeState(dataProvider, template));
        }

        for (int i = 0; i < this.nodeStates.size(); ++i)
        {
            nodeStates.get(i).parseConnections(nodeStates, animatorTemplate.nodes.get(i));
        }

        try
        {
            currentNode = nodeStates.get(animatorTemplate.entryNode);
        }
        catch(IndexOutOfBoundsException ex)
        {
            throw new MalformedKumoTemplateException("Entry node index is out of bounds");
        }
    }

    public INodeState getCurrentNode()
    {
        return currentNode;
    }

    @Nullable
    public INodeState update(D entityData, float deltaTime)
    {
        if (currentNode == null)
        {
            return null;
        }

        for (ConnectionState connection : currentNode.getConnections())
        {
            if (connection.triggerCondition.isConditionMet(entityData))
            {
                currentNode = connection.targetNode;
                currentNode.start();
                break;
            }
        }

        for (ILayerState layer : currentNode.getLayers())
        {
            layer.update(deltaTime);
        }

        return currentNode;
    }

    public void applyKeyframeAnimation(EntityData<?> entityData, KeyframeAnimation animation, float keyframeIndex)
    {
        int frameA = (int) keyframeIndex;
        int frameB = (int) keyframeIndex + 1;
        float tween = keyframeIndex - frameA;

        if (shouldPartBeAffected("root") && animation.bones.containsKey("root"))
        {
            Bone rootBone = animation.bones.get("root");
            Keyframe keyframe = rootBone.keyframes.get(frameA);
            Keyframe nextFrame = rootBone.keyframes.get(frameB);

            float x = keyframe.position[0] + (nextFrame.position[0] - keyframe.position[0]) * tween;
            float y = keyframe.position[1] + (nextFrame.position[1] - keyframe.position[1]) * tween;
            float z = keyframe.position[2] + (nextFrame.position[2] - keyframe.position[2]) * tween;

            entityData.globalOffset.set(x, y, z);
        }

        if (shouldPartBeAffected("centerRotation") && animation.bones.containsKey("centerRotation"))
        {
            Bone rootBone = animation.bones.get("centerRotation");
            Keyframe keyframe = rootBone.keyframes.get(frameA);
            Keyframe nextFrame = rootBone.keyframes.get(frameB);

            float x0 = keyframe.rotation[0];
            float y0 = keyframe.rotation[1];
            float z0 = keyframe.rotation[2];
            float w0 = keyframe.rotation[3];
            float x1 = nextFrame.rotation[0];
            float y1 = nextFrame.rotation[1];
            float z1 = nextFrame.rotation[2];
            float w1 = nextFrame.rotation[3];

            entityData.centerRotation.set(x0 + (x1 - x0) * tween,
                    y0 + (y1 - y0) * tween,
                    z0 + (z1 - z0) * tween,
                    w0 + (w1 - w0) * tween);

            float x = keyframe.position[0] + (nextFrame.position[0] - keyframe.position[0]) * tween;
            float y = keyframe.position[1] + (nextFrame.position[1] - keyframe.position[1]) * tween;
            float z = keyframe.position[2] + (nextFrame.position[2] - keyframe.position[2]) * tween;

            entityData.globalOffset.set(x, y, z);
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
                            IModelPart box = (IModelPart) part;
                            float x0 = keyframe.rotation[0];
                            float y0 = keyframe.rotation[1];
                            float z0 = keyframe.rotation[2];
                            float w0 = keyframe.rotation[3];
                            float x1 = nextFrame.rotation[0];
                            float y1 = nextFrame.rotation[1];
                            float z1 = nextFrame.rotation[2];
                            float w1 = nextFrame.rotation[3];

                            box.getRotation().set(x0 + (x1 - x0) * tween,
                                    y0 + (y1 - y0) * tween,
                                    z0 + (z1 - z0) * tween,
                                    w0 + (w1 - w0) * tween);
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

}
