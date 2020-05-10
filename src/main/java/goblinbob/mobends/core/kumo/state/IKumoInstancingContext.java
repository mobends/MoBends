package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;

/**
 * This is a context available during template instancing, which should provide all necessary instantiation data.
 *
 * @author Iwo Plaza
 */
public interface IKumoInstancingContext
{

    KeyframeAnimation getAnimation(String key);

}
