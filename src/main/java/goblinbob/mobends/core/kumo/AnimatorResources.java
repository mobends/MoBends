package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.animation.keyframe.AnimationLoader;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.util.GsonResources;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Instancing context backed by the resource packs: clips through {@link AnimationLoader},
 * animators (for {@code "extends"}) through {@link GsonResources}. Cached until
 * {@link #clearCache()} (resource reload).
 */
public class AnimatorResources implements IKumoInstancingContext
{

    public static final AnimatorResources INSTANCE = new AnimatorResources();

    private final Map<String, AnimatorTemplate> animators = new HashMap<>();

    public void clearCache()
    {
        animators.clear();
    }

    public AnimatorTemplate loadAnimator(ResourceLocation location) throws IOException
    {
        String key = location.toString();
        AnimatorTemplate template = animators.get(key);
        if (template == null)
        {
            template = GsonResources.get(location, AnimatorTemplate.class);
            animators.put(key, template);
        }
        return template;
    }

    @Override
    public KeyframeAnimation getAnimation(String key)
    {
        try
        {
            return AnimationLoader.loadFromPath(key);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Cannot load the animation clip '" + key + "'", e);
        }
    }

    @Override
    public AnimatorTemplate getAnimator(String key)
    {
        try
        {
            return loadAnimator(new ResourceLocation(key));
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Cannot load the animator '" + key + "'", e);
        }
    }

}
