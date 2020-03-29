package goblinbob.mobends.core.pack.state;

import goblinbob.mobends.core.animation.keyframe.Bone;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.pack.BendsPackData;
import goblinbob.mobends.core.pack.state.template.KeyframeLayerTemplate;

public class KeyframeLayerState implements ILayerState
{

    public final KeyframeAnimation animation;
    private int animationDuration;
    private final int startFrame;
    private final float playbackSpeed;
    private final boolean looping;

    /**
     * Progress counted in keyframes.
     */
    private float progress;

    public KeyframeLayerState(KeyframeAnimation animation, int startFrame, float playbackSpeed, boolean looping)
    {
        this.animation = animation;
        this.animationDuration = 0;
        this.startFrame = startFrame;
        this.playbackSpeed = playbackSpeed;
        this.looping = looping;

        for (Bone bone : this.animation.bones.values())
        {
            if (bone.keyframes.size() > this.animationDuration)
                this.animationDuration = bone.keyframes.size();
        }

        this.progress = this.startFrame;
    }

    @Override
    public void start()
    {
        this.progress = this.startFrame;
    }

    @Override
    public void update(float deltaTime)
    {
        if (this.looping)
        {
            this.progress += this.playbackSpeed * deltaTime;

            while (this.progress >= this.animationDuration - 1)
            {
                this.progress -= this.animationDuration - 1;
            }
        }
        else
        {
            if (this.progress < this.animationDuration - 2)
            {
                this.progress = Math.min(this.progress + this.playbackSpeed * deltaTime, animationDuration - 2);
            }
        }
    }

    @Override
    public boolean isAnimationFinished()
    {
        return !this.looping && this.progress >= animationDuration - 2;
    }

    public float getProgress()
    {
        return progress;
    }

    public static KeyframeLayerState createFromTemplate(BendsPackData data, KeyframeLayerTemplate layerTemplate)
    {
        return new KeyframeLayerState(
                data.keyframeAnimations.get(layerTemplate.animationKey),
                layerTemplate.startFrame,
                layerTemplate.playbackSpeed,
                layerTemplate.looping);
    }

}
