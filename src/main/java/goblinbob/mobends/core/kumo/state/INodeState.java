package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;

import java.util.List;

public interface INodeState
{

    Iterable<ConnectionState> getConnections();

    KeyframeAnimation getAnimation();

    /**
     * Returns progress counted in keyframes including the in-betweens (not just whole number indices).
     */
    float getProgress();

    boolean isAnimationFinished();

    void parseConnections(List<INodeState> nodeStates, KeyframeNodeTemplate template) throws MalformedKumoTemplateException;

    void start(IKumoContext context);

    void update(IKumoContext context, float deltaTime);

}
