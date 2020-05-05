package goblinbob.mobends.core.pack;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.state.IKumoDataProvider;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;

import java.util.Map;

public class BendsPackData implements IKumoDataProvider
{

    public Map<String, AnimatorTemplate> targets;
    public Map<String, KeyframeAnimation> keyframeAnimations;

    @Override
    public KeyframeAnimation getAnimation(String key)
    {
        return keyframeAnimations.get(key);
    }

}
