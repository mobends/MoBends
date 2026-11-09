package goblinbob.mobends.core.kumo;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.kumo.option.OptionSet;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.KumoProgram;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.ErrorReporter;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
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
 * <p>If the animator fails to load or while animating, the controller logs it and says it in chat
 * ({@link ErrorReporter}; once per animator, however many entities use it, until the animation
 * reloads) and animates nothing from then on, so a broken asset never crashes the render. An
 * extension that fails to load is left out, and said the same way.
 */
public class KumoAnimatorController {
    /** The keys of the chat notices ({@link ErrorReporter}) of animators and extensions that fail. */
    private static final String ANIMATOR_NOTICE = "animator ";
    private static final String EXTENSION_NOTICE = "extension ";
    /** The animators whose failure while animating was logged, until the animation reloads. */
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
    private final OptionSet typeOptions;
    private final List<OptionSet> extensionOptions;
    @Nullable
    private KumoAnimatorState state;
    private boolean failed;

    /** {@code animator}, reading {@code entity} (the scope a model definition declares; null for none). */
    public KumoAnimatorController(@Nullable EntityTemplate entity, ResourceLocation animator) {
        this(entity, animator, Collections.emptyList(), OptionSet.EMPTY, Collections.emptyList());
    }

    /**
     * {@code animator}, with the layers of each of {@code extensions} on top, in order, all of them
     * reading {@code entity}; with the options the animator reads besides the model definition's
     * (its type file's), and those each extension's animator reads (its extension file's).
     */
    public KumoAnimatorController(@Nullable EntityTemplate entity, ResourceLocation animator, List<ResourceLocation> extensions,
                                  OptionSet typeOptions, List<OptionSet> extensionOptions) {
        this.entity = entity;
        this.animator = animator;
        this.extensions = extensions;
        this.typeOptions = typeOptions;
        this.extensionOptions = extensionOptions;
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
        List<Object> key = Arrays.asList(animator, extensions, entity == null ? null : entity.entityClass,
                entity == null ? null : new IdentityKey(entity.define), entity == null ? null : new IdentityKey(entity.on),
                entity != null && entity.trusted, entity == null ? null : new IdentityKey(entity.options),
                new IdentityKey(typeOptions), identityKeys(extensionOptions));
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

    /** Compiles the animator and its extensions; the program, or why it failed (logged). */
    private Object compile() {
        String entityClass = entity == null ? "no entity" : entity.entityClass.getName();
        try {
            AnimatorResources resources = AnimatorResources.INSTANCE;
            List<ResourceLocation> loaded = new ArrayList<>();
            List<AnimatorTemplate> overlays = new ArrayList<>();
            List<Boolean> overlaysTrusted = new ArrayList<>();
            List<OptionSet> overlayOptions = new ArrayList<>();
            for (int i = 0; i < extensions.size(); i++) {
                ResourceLocation extension = extensions.get(i);
                // An extension that fails is left out; the animator and the other extensions still animate.
                try {
                    overlays.add(resources.loadAnimator(extension));
                    overlaysTrusted.add(resources.isTrusted(extension.toString()));
                    overlayOptions.add(extensionOptions.get(i));
                    loaded.add(extension);
                } catch (Exception e) {
                    skip(extension, e);
                }
            }
            // What the program compiles against can't change from here on.
            KumoRegistry.close();
            KumoProgram program = new KumoProgram(entity, resources.loadAnimator(animator), resources.isTrusted(animator.toString()), typeOptions,
                    overlays, overlaysTrusted, overlayOptions, resources);
            program.getSkippedExtensions().forEach((index, e) -> skip(loaded.get(index), e));
            MoBends.LOGGER.info("Mo' Bends: compiled {} for {}", describe(), entityClass);
            return program;
        } catch (Exception e) {
            MoBends.LOGGER.error("Mo' Bends: could not compile {} for {}: {}", describe(), entityClass, e.getMessage(), e);
            ErrorReporter.report(ANIMATOR_NOTICE + describe(), "The animator " + describe() + " could not be loaded: " + e.getMessage() + " (see the log).");
            return e;
        }
    }

    private String describe() {
        return animator + (extensions.isEmpty() ? "" : " with the extensions " + extensions);
    }

    private void skip(ResourceLocation extension, Exception e) {
        MoBends.LOGGER.error("Mo' Bends: could not load the extension {} of the animator {}: it is left out: {}", extension, animator, e.getMessage(), e);
        String key = extension + " on " + animator;
        ErrorReporter.report(EXTENSION_NOTICE + key, "The extension " + key + " could not be loaded, and is left out: " + e.getMessage() + " (see the log).");
    }

    private static List<IdentityKey> identityKeys(List<?> objects) {
        List<IdentityKey> keys = new ArrayList<>();
        for (Object object : objects) {
            keys.add(new IdentityKey(object));
        }
        return keys;
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
            if (REPORTED.add(describe())) {
                MoBends.LOGGER.error("Mo' Bends: the animator {} failed while animating: {}", describe(), e.getMessage(), e);
                ErrorReporter.report(ANIMATOR_NOTICE + describe(), "The animator " + describe() + " stopped working: " + e.getMessage() + " (see the log).");
            }
        }
    }

    /**
     * Forgets the compiled animators and which failures were logged and said: after a reload,
     * animators compile again from the new files, and failures are logged and said again.
     */
    public static void clearCaches() {
        REPORTED.clear();
        PROGRAMS.clear();
        ErrorReporter.forget(ANIMATOR_NOTICE);
        ErrorReporter.forget(EXTENSION_NOTICE);
    }
}
