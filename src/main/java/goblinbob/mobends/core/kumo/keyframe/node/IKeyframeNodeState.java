package goblinbob.mobends.core.kumo.keyframe.node;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoReadContext;
import goblinbob.mobends.core.kumo.INodeState;

public interface IKeyframeNodeState<D extends IEntityData> extends INodeState<D>
{
    KeyframeAnimation getAnimation();

    boolean isAnimationFinished(IKumoReadContext<D> context);

    /**
     * Returns progress counted in keyframes including the in-betweens (not just whole number indices).
     */
    float getProgress(IKumoReadContext<D> context);
}
