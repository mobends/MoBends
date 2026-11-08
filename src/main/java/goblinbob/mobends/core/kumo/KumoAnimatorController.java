package goblinbob.mobends.core.kumo;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.KumoProgram;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Animates one entity with an animator asset: every frame the animator's layers write the bone
 * targets of the entity data (which does the smoothing).
 *
 * <p>If the animator fails to load or while animating, the controller logs it (once per animator,
 * however many entities use it) and animates nothing from then on, so a broken asset never crashes
 * the render.
 */
public class KumoAnimatorController {
    /** The animators whose failure while animating was logged, until the animation reloads. */
    private static final Set<String> REPORTED = new HashSet<>();
    /**
     * The animators compiled, until the animation reloads: one program per animator and entity (its
     * class and entity scope), shared by every entity it animates; or why it failed, so it fails
     * once, not once per entity.
     */
    private static final Map<List<Object>, Object> PROGRAMS = new HashMap<>();

    @Nullable
    private final EntityTemplate entity;
    private final ResourceLocation animator;
    @Nullable
    private KumoAnimatorState state;
    private boolean failed;

    /** {@code animator}, reading {@code entity} (the scope a model definition declares; null for none). */
    public KumoAnimatorController(@Nullable EntityTemplate entity, ResourceLocation animator) {
        this.entity = entity;
        this.animator = animator;
    }

    public ResourceLocation getAnimator() {
        return animator;
    }

    private boolean ensureLoaded() {
        if (state != null) {
            return true;
        }
        if (failed) {
            return false;
        }
        List<Object> key = Arrays.asList(animator, entity == null ? null : entity.entityClass,
                entity == null ? null : new IdentityKey(entity.define), entity == null ? null : new IdentityKey(entity.on),
                entity != null && entity.trusted, entity == null ? null : new IdentityKey(entity.options));
        Object compiled = PROGRAMS.get(key);
        if (compiled == null) {
            compiled = compile();
            PROGRAMS.put(key, compiled);
        }
        if (compiled instanceof Exception) {
            failed = true;
            return false;
        }
        state = new KumoAnimatorState((KumoProgram) compiled);
        return true;
    }

    /** Compiles the animator; the program, or why it failed (logged). */
    private Object compile() {
        String entityClass = entity == null ? "no entity" : entity.entityClass.getName();
        try {
            AnimatorResources resources = AnimatorResources.INSTANCE;
            // What the program compiles against can't change from here on.
            KumoRegistry.close();
            KumoProgram program = new KumoProgram(entity, resources.loadAnimator(animator), resources.isTrusted(animator.toString()),
                    Collections.emptyList(), Collections.emptyList(), resources);
            MoBends.LOGGER.info("Mo' Bends: compiled {} for {}", animator, entityClass);
            return program;
        } catch (Exception e) {
            MoBends.LOGGER.error("Mo' Bends: could not compile {} for {}: {}", animator, entityClass, e.getMessage(), e);
            return e;
        }
    }

    /** A key part equal only to the very same object (a model definition's scope, loaded once). */
    private record IdentityKey(Object object) {
        @Override
        public boolean equals(Object other) {
            return other instanceof IdentityKey key && key.object == object;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(object);
        }
    }

    /** True while the animator asks for the vanilla model and animation (a {@code core:vanilla} node). */
    public boolean wantsVanilla() {
        return state != null && state.wantsVanilla();
    }

    /** Animates {@code subject} for this frame. */
    public void animate(IKumoSubject subject) {
        if (!ensureLoaded()) {
            return;
        }
        try {
            state.setLimits(AnimationLimits.current());
            state.update(subject, DataUpdateHandler.ticksPerFrame);
        } catch (MalformedKumoTemplateException e) {
            failed = true;
            state = null;
            if (REPORTED.add(animator.toString())) {
                MoBends.LOGGER.error("Mo' Bends: the animator {} failed while animating: {}", animator, e.getMessage(), e);
            }
        }
    }

    /**
     * Forgets the compiled animators and which failures were logged: after a reload, animators
     * compile again from the new files, and failures are logged again.
     */
    public static void clearCaches() {
        REPORTED.clear();
        PROGRAMS.clear();
    }
}
