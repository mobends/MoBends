package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Animates one entity with an animator asset: every frame the animator's layers write the bone
 * targets of the entity data (which does the smoothing). Extensions add the layers of their own
 * animators on top.
 *
 * <p>If the animator fails to load or while animating, the controller logs once and animates
 * nothing from then on, so a broken asset never crashes the render.
 */
public class KumoAnimatorController
{

    private final ResourceLocation animator;
    private final List<ResourceLocation> extensions;
    @Nullable
    private KumoAnimatorState state;
    private boolean failed;

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
            Core.LOG.log(Level.SEVERE, "Could not load the animator " + animator + (extensions.isEmpty() ? "" : " with the extensions " + extensions), e);
            return false;
        }
    }

    /** True while the animator asks for the vanilla model and animation (a {@code core:vanilla} node). */
    public boolean wantsVanilla()
    {
        return state != null && state.wantsVanilla();
    }

    /** Animates {@code subject} for this frame. */
    public void animate(IKumoSubject subject)
    {
        if (!ensureLoaded())
        {
            return;
        }
        try
        {
            state.setLimits(AnimationLimits.current());
            state.update(subject, DataUpdateHandler.ticksPerFrame);
        }
        catch (MalformedKumoTemplateException e)
        {
            failed = true;
            state = null;
            Core.LOG.log(Level.SEVERE, "The animator " + animator + " failed while animating", e);
        }
    }

}
