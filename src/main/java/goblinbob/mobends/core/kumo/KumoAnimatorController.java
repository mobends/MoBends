package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.KumoProgram;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.ErrorReporter;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    /**
     * The animators compiled, until the animation reloads: one program per animator, extensions and
     * entity (its class and entity scope), shared by every entity it animates; or why it failed,
     * so it fails once, not once per entity.
     */
    private static final Map<List<Object>, Object> PROGRAMS = new HashMap<>();

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
        List<Object> key = Arrays.asList(animator, extensions, entity == null ? null : entity.entityClass,
                entity == null ? null : new IdentityKey(entity.define), entity == null ? null : new IdentityKey(entity.on), entity != null && entity.trusted);
        Object compiled = PROGRAMS.get(key);
        if (compiled == null)
        {
            compiled = compile();
            PROGRAMS.put(key, compiled);
        }
        if (compiled instanceof Exception)
        {
            failed = true;
            return false;
        }
        state = new KumoAnimatorState((KumoProgram) compiled);
        return true;
    }

    /** Compiles the animator and its extensions; the program, or why it failed (logged and shown). */
    private Object compile()
    {
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
            KumoProgram program = new KumoProgram(entity, resources.loadAnimator(animator), resources.isTrusted(animator.toString()), overlays, overlaysTrusted, resources);
            program.getSkippedExtensions().forEach((index, e) -> skip(loaded.get(index), e));
            return program;
        }
        catch (Exception e)
        {
            Core.LOG.log(Level.SEVERE, "Could not load the animator " + describe(), e);
            report("could not be loaded", e);
            return e;
        }
    }

    /** A key part equal only to the very same object (a model definition's scope, loaded once). */
    private static final class IdentityKey
    {
        private final Object object;

        IdentityKey(Object object)
        {
            this.object = object;
        }

        @Override
        public boolean equals(Object other)
        {
            return other instanceof IdentityKey && ((IdentityKey) other).object == object;
        }

        @Override
        public int hashCode()
        {
            return System.identityHashCode(object);
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

    /**
     * Forgets the compiled animators and which failures were shown: after a reload (or a change of
     * the server's policy), animators compile again from the new files, and failures show again.
     */
    public static void clearCaches()
    {
        REPORTED.clear();
        PROGRAMS.clear();
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
