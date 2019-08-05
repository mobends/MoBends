package net.gobbob.mobends.core.pack.state;

import net.gobbob.mobends.core.animation.keyframe.Bone;
import net.gobbob.mobends.core.animation.keyframe.KeyframeAnimation;
import net.gobbob.mobends.core.pack.BendsPackData;
import net.gobbob.mobends.core.pack.state.template.KeyframeLayerTemplate;

public class KeyframeLayerState implements ILayerState
{

    public final KeyframeAnimation animation;
    private int animationDuration;
    private final int startFrame = 0;

    /**
     * Progress counted in keyframes.
     */
    private float progress;

    public KeyframeLayerState(KeyframeAnimation animation)
    {
        this.animation = animation;
        this.animationDuration = 0;
        for (Bone bone : this.animation.bones.values())
        {
            if (bone.keyframes.size() > this.animationDuration)
                this.animationDuration = bone.keyframes.size();
        }
        this.progress = 0;
    }

    @Override
    public void update(float deltaTime)
    {
        this.progress += deltaTime;
        while (this.progress >= this.animationDuration - 1)
            this.progress -= this.animationDuration - 1;
    }

    public float getProgress()
    {
        return progress;
    }

    public static KeyframeLayerState createFromTemplate(BendsPackData data, KeyframeLayerTemplate layerTemplate)
    {
        return new KeyframeLayerState(data.keyframeAnimations.get(layerTemplate.animationKey));
    }

}
