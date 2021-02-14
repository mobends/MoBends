package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;

import java.util.List;

public interface INodeState<D extends IEntityData>
{
    Iterable<ConnectionState<D>> getConnections();

    KeyframeAnimation getAnimation();

    /**
     * Returns progress counted in keyframes including the in-betweens (not just whole number indices).
     */
    float getProgress(IKumoReadContext<D> context);

    boolean isAnimationFinished(IKumoReadContext<D> context);

    void parseConnections(List<INodeState<D>> nodeStates, KeyframeNodeTemplate template, IKumoInstancingContext<D> context);

    void start(IKumoContext<D> context);

    void update(IKumoContext<D> context);
}
