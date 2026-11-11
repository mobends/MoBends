package goblinbob.mobends.core.client.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.definition.EntityModelDefinition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * The render layers a model definition asks for in its {@code layers} block, by id
 * ({@code {"mobends:held_item": {}, "mobends:custom_head": {"bone": "head"}}}), as 1.12's
 * {@code DefinedLayers}. In 1.12 each id swapped one of the renderer's vanilla layers for Mo' Bends'
 * own; here the vanilla layer stays, and the id says that the vanilla layers of its class, drawn
 * for a bent entity of that definition, follow the bones ({@code BendsLayers}): a layer the
 * definition doesn't name draws as vanilla does, as 1.12's did. A mod adds its own kinds with
 * {@link #register}.
 * <p>
 * Some layers draw their own copy of the entity's model that no 1.12 definition names (the
 * drowned's outer layer and the wolf's armour, new since 1.12; the pig's saddle). A copy left
 * unbent would show the model twice in two poses, so they are bent with every definition that
 * bends their entity ({@link #registerImplicit}).
 */
public final class DefinedLayers {
    public static final String ARMOR = "mobends:armor";
    public static final String HELD_ITEM = "mobends:held_item";
    public static final String CUSTOM_HEAD = "mobends:custom_head";
    public static final String CAPE = "mobends:cape";
    public static final String ELYTRA = "mobends:elytra";
    public static final String ACCESSORIES = "mobends:accessories";

    /** What Mo' Bends does to a vanilla layer of a kind. */
    public enum Role {
        /** Moves what the layer draws onto the bones; {@code BendsLayers} does it for each id it knows. */
        ON_BONES,
        /**
         * The layer draws its own copy of the entity's model (1.12's {@code AnimatedCopyLayer}): the
         * copy is drawn bent as the entity is, posed by its data, cut where its parts are.
         */
        COPY
    }

    /**
     * A kind of layer: its id, the vanilla layer class (and subclasses) it applies to, and its
     * role. A kind with no class is known but has nothing to apply to on this version.
     */
    public record Kind(String id, @Nullable Class<?> applies, Role role) {
    }

    /** A kind as a definition asks for it, with the options it gives it. */
    public record Layer(Kind kind, JsonObject options) {
        public boolean is(String id) {
            return kind.id.equals(id);
        }

        /** The option {@code name} as a string, or {@code fallback} if the definition doesn't give it. */
        public String option(String name, String fallback) {
            JsonElement value = options.get(name);
            return value != null && value.isJsonPrimitive() ? value.getAsString() : fallback;
        }
    }

    private static final Map<String, Kind> KINDS = new HashMap<>();
    private static final List<Kind> IMPLICIT = new ArrayList<>();
    /** Per definition (loaded once per reload), per vanilla layer class: the layer it asks for there. */
    private static final Map<EntityModelDefinition, Map<Class<?>, Optional<Layer>>> CACHE = new WeakHashMap<>();
    private static final JsonObject NO_OPTIONS = new JsonObject();

    private DefinedLayers() {
    }

    /** A kind whose layers have what they draw moved onto the bones. */
    public static void register(String id, @Nullable Class<?> applies) {
        KINDS.put(id, new Kind(id, applies, Role.ON_BONES));
    }

    /** A kind whose layers draw their own copy of the entity's model, which is drawn bent. */
    public static void registerCopy(String id, Class<?> applies) {
        KINDS.put(id, new Kind(id, applies, Role.COPY));
    }

    /** A kind that applies to every definition, whether its {@code layers} name it or not (see the class's notes). */
    public static void registerImplicit(String id, Class<?> applies, Role role) {
        Kind kind = new Kind(id, applies, role);
        KINDS.put(id, kind);
        IMPLICIT.add(kind);
    }

    /** Forgets what the definitions ask for: they are read again (after a resource reload). */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * The layer {@code definition} asks for in place of a vanilla layer of class {@code layerClass},
     * or null to leave that vanilla layer as it is.
     */
    @Nullable
    public static Layer of(EntityModelDefinition definition, Class<?> layerClass) {
        Map<Class<?>, Optional<Layer>> byClass = CACHE.get(definition);
        if (byClass == null) {
            byClass = new HashMap<>();
            CACHE.put(definition, byClass);
            warnUnknown(definition);
        }
        return byClass.computeIfAbsent(layerClass, c -> Optional.ofNullable(resolve(definition, c))).orElse(null);
    }

    @Nullable
    private static Layer resolve(EntityModelDefinition definition, Class<?> layerClass) {
        if (definition.layers != null) {
            for (Map.Entry<String, JsonObject> entry : definition.layers.entrySet()) {
                Kind kind = KINDS.get(entry.getKey());
                if (kind != null && kind.applies != null && kind.applies.isAssignableFrom(layerClass)) {
                    return new Layer(kind, entry.getValue() != null ? entry.getValue() : NO_OPTIONS);
                }
            }
        }
        for (Kind kind : IMPLICIT) {
            if (kind.applies != null && kind.applies.isAssignableFrom(layerClass)) {
                return new Layer(kind, NO_OPTIONS);
            }
        }
        return null;
    }

    /** Said once per definition (per reload): it names a layer nobody registered. */
    private static void warnUnknown(EntityModelDefinition definition) {
        if (definition.layers == null) {
            return;
        }
        for (String id : definition.layers.keySet()) {
            //? if >=26.1 {
            /*// Mo' Bends' own layer not ported to 26.x yet: left out without a word.
            if (id.equals("mobends:accessories")) {
                continue;
            }
            *///?}
            if (!KINDS.containsKey(id)) {
                MoBends.LOGGER.warn("Model definition {}: there is no layer '{}'; it is left out.", definition.location, id);
            }
        }
    }
}
