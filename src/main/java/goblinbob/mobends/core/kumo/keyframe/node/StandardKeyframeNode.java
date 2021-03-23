package goblinbob.mobends.core.kumo.keyframe.node;

import goblinbob.mobends.core.animation.keyframe.Bone;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.*;

public class StandardKeyframeNode<D extends IEntityData> extends NodeState<D> implements IKeyframeNodeState<D>
{
    public final KeyframeAnimation animation;
    private int animationDuration;
    private final int startFrame;
    private final float playbackSpeed;
    private final boolean looping;

    /**
     * Holds the time (in ticks) when the animation was started.
     */
    private float animationStartTime;

    public StandardKeyframeNode(IKumoInstancingContext<D> context, StandardKeyframeNodeTemplate nodeTemplate)
    {
        this(nodeTemplate.animationKey != null ? context.getAnimation(nodeTemplate.animationKey) : null,
                nodeTemplate.startFrame,
                nodeTemplate.playbackSpeed,
                nodeTemplate.looping);
    }

    public StandardKeyframeNode(KeyframeAnimation animation, int startFrame, float playbackSpeed, boolean looping)
    {
        this.animation = animation;
        this.startFrame = startFrame;
        this.playbackSpeed = playbackSpeed;
        this.looping = looping;

        if (animation != null)
        {
            // Evaluating the animation duration.
            this.animationDuration = 0;
            for (Bone bone : animation.bones.values())
            {
                if (bone.getKeyframes().length > this.animationDuration)
                    this.animationDuration = bone.getKeyframes().length;
            }
        }
    }

    @Override
    public void start(IKumoContext<D> context)
    {
        float ticksOffset = this.startFrame / this.playbackSpeed;
        this.animationStartTime = context.getTicksPassed() - ticksOffset;

        for (ConnectionState<D> connection : connections)
        {
            connection.triggerCondition.onNodeStarted(context);
        }
    }

    @Override
    public void update(IKumoContext<D> context)
    {
    }

    @Override
    public KeyframeAnimation getAnimation()
    {
        return animation;
    }

    @Override
    public boolean isAnimationFinished(IKumoReadContext<D> context)
    {
        if (this.animation == null)
        {
            return true;
        }

        if (this.looping)
        {
            return false;
        }

        float ticksPlayed = (context.getTicksPassed() - animationStartTime);
        return ticksPlayed * playbackSpeed >= animationDuration;
    }

    /**
     * Returns progress counted in keyframes including the in-betweens (not just whole number indices).
     */
    @Override
    public float getProgress(IKumoReadContext<D> context)
    {
        float frames = (context.getTicksPassed() - animationStartTime) * playbackSpeed;
        if (looping)
        {
            return frames % (animationDuration - 1);
        }
        else
        {
            return Math.min(frames, animationDuration - 2);
        }
    }
}
