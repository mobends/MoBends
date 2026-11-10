package goblinbob.mobends.core.kumo;

import com.google.gson.Gson;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.client.PackTrust;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.util.GsonResources;
import goblinbob.mobends.core.util.Ids;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Instancing context backed by the resource packs: animators (and their {@code "extends"}) and
 * clips, cached until {@link #clearCache()} (a resource reload). What a resource pack the player
 * enabled supplies is untrusted ({@link PackTrust}).
 */
public class AnimatorResources implements IKumoInstancingContext {
    public static final AnimatorResources INSTANCE = new AnimatorResources();

    private static final Gson CLIP_GSON = new Gson();

    private final Map<ResourceLocation, AnimatorTemplate> animators = new HashMap<>();
    private final Map<ResourceLocation, KeyframeAnimation> clips = new HashMap<>();

    public void clearCache() {
        animators.clear();
        clips.clear();
    }

    public AnimatorTemplate loadAnimator(ResourceLocation location) throws IOException {
        AnimatorTemplate template = animators.get(location);
        if (template == null) {
            template = GsonResources.read(location, KumoSerializer.INSTANCE.gson, AnimatorTemplate.class);
            animators.put(location, template);
        }
        return template;
    }

    public KeyframeAnimation loadClip(ResourceLocation location) throws IOException {
        KeyframeAnimation clip = clips.get(location);
        if (clip == null) {
            clip = GsonResources.read(location, CLIP_GSON, KeyframeAnimation.class, "clip", KeyframeAnimation.FORMAT_VERSION);
            clips.put(location, clip);
        }
        return clip;
    }

    /** The clip at a resource key ({@code modid:path}); null when it can't be loaded (the animator then fails). */
    @Override
    public KeyframeAnimation getAnimation(String key) {
        try {
            return loadClip(Ids.parse(key));
        } catch (IOException | ResourceLocationException e) {
            return null;
        }
    }

    /** Whether the version of the animator or clip at a resource key that's read is trusted (see {@link PackTrust}). */
    @Override
    public boolean isTrusted(String key) {
        try {
            return PackTrust.opensTrusted(Ids.parse(key));
        } catch (ResourceLocationException e) {
            return true;
        }
    }

    /** The animator at a resource key; null when it can't be loaded (the animator extending it then fails). */
    @Override
    public AnimatorTemplate getAnimator(String key) {
        try {
            return loadAnimator(Ids.parse(key));
        } catch (IOException | ResourceLocationException e) {
            return null;
        }
    }
}
