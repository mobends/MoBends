package net.gobbob.mobends.core.pack;

import net.gobbob.mobends.core.animation.keyframe.Bone;
import net.gobbob.mobends.core.animation.keyframe.Keyframe;
import net.gobbob.mobends.core.animation.keyframe.KeyframeAnimation;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.client.model.IModelPart;
import net.gobbob.mobends.core.data.EntityData;
import net.gobbob.mobends.core.pack.state.ILayerState;
import net.gobbob.mobends.core.pack.state.INodeState;
import net.gobbob.mobends.core.pack.state.KeyframeLayerState;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public class BendsPackPerformer
{

    public static final BendsPackPerformer instance = new BendsPackPerformer();

    public void performCurrentPack(EntityData<?> entityData, @Nullable Collection<String> actions)
    {
        IBendsPack pack = PackManager.instance.getAppliedPack();
        if (pack == null)
        {
            return;
        }

        BendsPackData packData = PackProvider.instance.getDataForPack(PackManager.instance.getAppliedPack());
        if (packData == null)
        {
            return;
        }

        INodeState nodeState = entityData.packAnimationState.update(packData, DataUpdateHandler.ticksPerFrame);
        if (nodeState != null)
        {
            Iterable<ILayerState> layers = nodeState.getLayers();
            for (ILayerState layer : layers)
            {
                if (layer instanceof KeyframeLayerState)
                {
                    KeyframeLayerState keyframeLayer = (KeyframeLayerState) layer;
                    applyKeyframeAnimation(entityData, keyframeLayer.animation, keyframeLayer.getProgress());
                }
            }
        }
    }

    public void applyKeyframeAnimation(EntityData<?> entityData, KeyframeAnimation animation, float keyframeIndex)
    {
        int frameA = (int) keyframeIndex;
        int frameB = (int) keyframeIndex + 1;
        float tween = keyframeIndex - frameA;

        if (animation.bones.containsKey("root") && shouldPartBeAffected("root"))
        {
            Bone rootBone = animation.bones.get("root");
            Keyframe keyframe = rootBone.keyframes.get(frameA);
            Keyframe nextFrame = rootBone.keyframes.get(frameB);

            float x = keyframe.position[0] + (nextFrame.position[0] - keyframe.position[0]) * tween;
            float y = keyframe.position[1] + (nextFrame.position[1] - keyframe.position[1]) * tween;
            float z = keyframe.position[2] + (nextFrame.position[2] - keyframe.position[2]) * tween;

            entityData.renderOffset.set(z, x, y);
        }

        for (Map.Entry<String, Bone> entry : animation.bones.entrySet())
        {
            Bone bone = entry.getValue();
            Object part = entityData.getPartForName(entry.getKey());

            if (part != null && shouldPartBeAffected(entry.getKey()))
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

    private boolean shouldPartBeAffected(String partName)
    {
        return true;
    }

}
