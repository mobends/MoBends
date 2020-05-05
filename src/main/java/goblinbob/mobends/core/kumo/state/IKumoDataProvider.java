package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;

public interface IKumoDataProvider
{

    KeyframeAnimation getAnimation(String key);

}
