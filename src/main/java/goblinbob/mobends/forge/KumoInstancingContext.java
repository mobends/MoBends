package goblinbob.mobends.forge;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import java.io.IOException;

public class KumoInstancingContext implements IKumoInstancingContext<EntityData>
{
    @Override
    public KeyframeAnimation getAnimation(String key)
    {
        try
        {
            return AnimationLoader.loadFromPath(key);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
