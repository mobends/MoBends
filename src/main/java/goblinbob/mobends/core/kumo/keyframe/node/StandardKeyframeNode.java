package goblinbob.mobends.core.kumo.keyframe.node;

import goblinbob.mobends.core.animation.keyframe.Bone;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.*;

import java.util.ArrayList;
import java.util.List;

public class StandardKeyframeNode<D extends IEntityData> implements INodeState<D>
{
    public final KeyframeAnimation animation;
    private int animationDuration;
    private final int startFrame;
    private final float playbackSpeed;
    private final boolean looping;
    List<ConnectionState<D>> connections = new ArrayList<>();

    /**
     * Progress counted in keyframes.
     */
    private float progress;

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
                if (bone.keyframes.size() > this.animationDuration)
                    this.animationDuration = bone.keyframes.size();
            }
        }

        this.progress = this.startFrame;
    }

    public void parseConnections(List<INodeState<D>> nodeStates, KeyframeNodeTemplate template, IKumoInstancingContext<D> context) throws MalformedKumoTemplateException
    {
        if (template.connections != null)
        {
            for (ConnectionTemplate connectionTemplate : template.connections)
            {
                this.connections.add(ConnectionState.createFromTemplate(nodeStates, connectionTemplate));
            }
        }
    }

    @Override
    public void start(IKumoContext<D> context)
    {
        this.progress = this.startFrame;
        for (ConnectionState<D> connection : connections)
        {
            connection.triggerCondition.onNodeStarted(context);
        }
    }

    @Override
    public void update(IKumoContext<D> context, float deltaTime)
    {
        if (animation != null)
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
    }

    @Override
    public KeyframeAnimation getAnimation()
    {
        return animation;
    }

    @Override
    public boolean isAnimationFinished()
    {
        return this.animation == null || !this.looping && this.progress >= animationDuration - 2;
    }

    /**
     * Returns progress counted in keyframes including the in-betweens (not just whole number indices).
     */
    public float getProgress()
    {
        return progress;
    }

    @Override
    public Iterable<ConnectionState<D>> getConnections()
    {
        return connections;
    }
}
