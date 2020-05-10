package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.NodeTemplate;

import java.util.List;

public interface INodeState
{

    void parseConnections(List<INodeState> nodeStates, NodeTemplate template) throws MalformedKumoTemplateException;

    Iterable<ConnectionState> getConnections();

    KeyframeAnimation getAnimation();

    /**
     * Returns progress counted in keyframes including the in-betweens (not just whole number indices).
     */
    float getProgress();

    boolean isAnimationFinished();

    void start();

    void update(float deltaTime);

}
