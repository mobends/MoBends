package goblinbob.mobends.core.kumo.state.keyframe;

import goblinbob.mobends.core.animation.keyframe.Bone;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.kumo.state.ConnectionState;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.ConnectionTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.MovementKeyframeNodeTemplate;

import java.util.ArrayList;
import java.util.List;

public class MovementKeyframeNode implements INodeState
{

    public final KeyframeAnimation animation;
    private int animationDuration;
    private final int startFrame;
    private final float playbackSpeed;
    List<ConnectionState> connections = new ArrayList<>();

    /**
     * Progress counted in keyframes.
     */
    private float progress;

    public MovementKeyframeNode(IKumoInstancingContext context, MovementKeyframeNodeTemplate nodeTemplate)
    {
        this(nodeTemplate.animationKey != null ? context.getAnimation(nodeTemplate.animationKey) : null,
                nodeTemplate.startFrame,
                nodeTemplate.playbackSpeed);
    }

    public MovementKeyframeNode(KeyframeAnimation animation, int startFrame, float playbackSpeed)
    {
        this.animation = animation;
        this.startFrame = startFrame;
        this.playbackSpeed = playbackSpeed;

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

    @Override
    public Iterable<ConnectionState> getConnections()
    {
        return connections;
    }

    @Override
    public KeyframeAnimation getAnimation()
    {
        return animation;
    }

    @Override
    public float getProgress()
    {
        return progress;
    }

    @Override
    public boolean isAnimationFinished()
    {
        return false;
    }

    @Override
    public void parseConnections(List<INodeState> nodeStates, KeyframeNodeTemplate template) throws MalformedKumoTemplateException
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
    public void start()
    {
        this.progress = this.startFrame;
    }

    @Override
    public void update(IKumoContext context, float deltaTime)
    {
        LivingEntityData<?> data = (LivingEntityData<?>) context.getEntityData();

        if (animation != null)
        {
            final float PI = (float) Math.PI;
            float limbSwing = data.limbSwing.get() * 0.6662F;
            float limbSwingAmount = data.limbSwingAmount.get() * 0.5F / PI * 180F;

            this.progress = this.playbackSpeed * limbSwing;
            this.progress %= this.animationDuration - 1;
        }
    }

}
