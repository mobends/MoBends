package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.ErrorReporter;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Animates one entity with an animator asset: every frame the animator's layers write the bone
 * targets of the entity data (which does the smoothing). Extensions add the layers of their own
 * animators on top.
 *
 * <p>If the animator fails to load or while animating, the controller logs it, tells the player in
 * the chat (once per animator, however many entities use it) and animates nothing from then on, so
 * a broken asset never crashes the render.
 */
public class KumoAnimatorController
{

    /** The animators (with their extensions) the player has been told failed, until the animation reloads. */
    private static final Set<String> REPORTED = new HashSet<>();

    @Nullable
    private final EntityTemplate entity;
    private final ResourceLocation animator;
    private final List<ResourceLocation> extensions;
    @Nullable
    private KumoAnimatorState state;
    private boolean failed;

    /**
     * {@code animator}, with the layers of each of {@code extensions} on top, in order, all of
     * them reading {@code entity} (the scope a model definition declares; null for none).
     */
    public KumoAnimatorController(@Nullable EntityTemplate entity, ResourceLocation animator, List<ResourceLocation> extensions)
    {
        this.entity = entity;
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
            List<ResourceLocation> loaded = new ArrayList<>();
            List<AnimatorTemplate> overlays = new ArrayList<>();
            List<Boolean> overlaysTrusted = new ArrayList<>();
            for (ResourceLocation extension : extensions)
            {
                // An extension that fails is left out; the animator and the other extensions still animate.
                try
                {
                    overlays.add(resources.loadAnimator(extension));
                    overlaysTrusted.add(resources.isTrusted(extension.toString()));
                    loaded.add(extension);
                }
                catch (Exception e)
                {
                    skip(extension, e);
                }
            }
            KumoRegistry.close();
            state = new KumoAnimatorState(entity, resources.loadAnimator(animator), resources.isTrusted(animator.toString()), overlays, overlaysTrusted, resources);
            state.getSkippedExtensions().forEach((index, e) -> skip(loaded.get(index), e));
            return true;
        }
        catch (Exception e)
        {
            failed = true;
            Core.LOG.log(Level.SEVERE, "Could not load the animator " + describe(), e);
            report("could not be loaded", e);
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
            Core.LOG.log(Level.SEVERE, "The animator " + describe() + " failed while animating", e);
            report("stopped working", e);
        }
    }

    /** Forgets which failures were shown, so they show again if they happen after a reload. */
    public static void clearReported()
    {
        REPORTED.clear();
    }

    private String describe()
    {
        return animator + (extensions.isEmpty() ? "" : " with the extensions " + extensions);
    }

    private void skip(ResourceLocation extension, Exception e)
    {
        Core.LOG.log(Level.SEVERE, "Could not load the extension " + extension + " of the animator " + animator + ": it is left out", e);
        String key = extension + " on " + animator;
        if (!REPORTED.contains(key) && ErrorReporter.showErrorToPlayer("The extension " + key + " could not be loaded, and is left out: " + e.getMessage() + " (see the log)."))
        {
            REPORTED.add(key);
        }
    }

    private void report(String what, Exception e)
    {
        String key = describe();
        // Not marked as shown while there is no player to show it to: the next entity tries again.
        if (!REPORTED.contains(key) && ErrorReporter.showErrorToPlayer("The animator " + key + " " + what + ": " + e.getMessage() + " (see the log)."))
        {
            REPORTED.add(key);
        }
    }

}
