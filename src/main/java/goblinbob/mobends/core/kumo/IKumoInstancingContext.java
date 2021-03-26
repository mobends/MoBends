package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.driver.IDriverFunctionProvider;

/**
 * This is a context available during template instancing, which should provide all necessary instantiation data.
 *
 * @author Iwo Plaza
 */
public interface IKumoInstancingContext<D extends IEntityData> extends IDriverFunctionProvider<D>
{
    KeyframeAnimation getAnimation(String key);
}
