package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An animation controller that is entirely an animator asset: every frame the animator's layers
 * write the bone targets of the entity data (which does the smoothing). Extensions add the layers
 * of their own animators on top.
 *
 * <p>If the animator fails to load, the controller logs once and animates nothing, so a broken
 * asset never crashes the render.
 */
public class KumoAnimatorController<T extends EntityData<?>> implements IAnimationController<T>
{

    private final ResourceLocation animator;
    private final List<ResourceLocation> extensions;
    @Nullable
    private KumoAnimatorState state;
    private boolean failed;

    public KumoAnimatorController(ResourceLocation animator)
    {
        this(animator, Collections.emptyList());
    }

    /** {@code animator}, with the layers of each of {@code extensions} on top, in order. */
    public KumoAnimatorController(ResourceLocation animator, List<ResourceLocation> extensions)
    {
        this.animator = animator;
        this.extensions = extensions;
    }

    public ResourceLocation getAnimator()
    {
        return animator;
    }

    public KumoAnimatorController(String modId, String path)
    {
        this(new ResourceLocation(modId, path));
    }

    @Nullable
    public KumoAnimatorState getState()
    {
        return state;
    }

    private boolean ensureLoaded()
    {
        if (state != null)
        {
            return true;
        }
        if (failed)
        {
            return false;
        }
        try
        {
            AnimatorResources resources = AnimatorResources.INSTANCE;
            List<AnimatorTemplate> overlays = new ArrayList<>();
            List<Boolean> overlaysTrusted = new ArrayList<>();
            for (ResourceLocation extension : extensions)
            {
                overlays.add(resources.loadAnimator(extension));
                overlaysTrusted.add(resources.isTrusted(extension.toString()));
            }
            state = new KumoAnimatorState(resources.loadAnimator(animator), resources.isTrusted(animator.toString()), overlays, overlaysTrusted, resources);
            return true;
        }
        catch (Exception e)
        {
            failed = true;
            Core.LOG.log(java.util.logging.Level.SEVERE, "Could not load the animator " + animator + (extensions.isEmpty() ? "" : " with the extensions " + extensions), e);
            return false;
        }
    }

    @Override
    public boolean wantsVanilla()
    {
        return state != null && state.wantsVanilla();
    }

    /** Drops the instanced animator so it is rebuilt from (reloaded) resources on the next frame. */
    public void reload()
    {
        state = null;
        failed = false;
    }

    @Nullable
    @Override
    public Collection<String> perform(T entityData)
    {
        if (!ensureLoaded())
        {
            return null;
        }
        try
        {
            state.setLimits(AnimationLimits.current());
            state.update(entityData, DataUpdateHandler.ticksPerFrame);
        }
        catch (MalformedKumoTemplateException e)
        {
            failed = true;
            state = null;
            Core.LOG.log(java.util.logging.Level.SEVERE, "The animator " + animator + " failed while animating", e);
            return null;
        }
        return state.getActions();
    }

}
