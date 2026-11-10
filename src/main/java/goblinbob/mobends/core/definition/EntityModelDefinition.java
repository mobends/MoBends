package goblinbob.mobends.core.definition;

import com.google.gson.JsonObject;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import goblinbob.mobends.core.kumo.option.KumoOptions;
import goblinbob.mobends.core.kumo.option.OptionSet;
import goblinbob.mobends.core.kumo.option.OptionTemplate;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.template.DefinitionTemplate;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.OnTemplate;
import goblinbob.mobends.core.vanilla.VanillaNames;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * A mob described as data (`assets/mobends/bends/models/<mob>.json`): which entity, which vanilla
 * model parts become bones (optionally split into more segments for extra joints), which animator
 * drives them, and the entity scope its animators read ({@code entity.x}). The generic data class, mutator and
 * renderer are built from it, so a mob that never had a Mo' Bends treatment needs only this file
 * and an animator.
 */
public class EntityModelDefinition {
    /** The {@code formatVersion} a model definition has to have (see {@link goblinbob.mobends.core.util.FormatVersion}). */
    public static final int FORMAT_VERSION = 2;

    /**
     * The entities it's for, one name or a list ({@code "entity": ["minecraft:fox", ...]}), each:
     * <ul>
     *   <li>a registry id ({@code minecraft:fox}, {@code mymod:my_mob}): the entities of that type
     *   alone. Ids don't change between versions or loaders, so they name any mob, those added after
     *   1.12 too;</li>
     *   <li>a full class name: Minecraft's by its 1.12 name (see
     *   {@link goblinbob.mobends.core.vanilla.VanillaNames}), another mod's by its own; the entities of
     *   that class and its subclasses.</li>
     * </ul>
     * An entity any of them names is one of its. Names this version doesn't know (another version's
     * mob, a mod that isn't installed) are skipped.
     */
    @JsonAdapter(EntityNamesDeserializer.class)
    public List<String> entity;

    /**
     * Optional: only models of this class (or a subclass) are bent. Minecraft's by its 1.12 name, as
     * {@link #entity} is; another mod's by its full name, which loaders don't rename (see
     * {@link #modelClass()}).
     */
    public String model;
    /** {@link #model}'s class, once {@link #modelClass()} found it. */
    private transient Class<?> modelClass;
    private transient boolean modelClassFound;

    /** Optional custom bender key / language key; defaults come from the entity registry. */
    public String key;
    public String unlocalizedName;

    /** The animator asset, e.g. {@code mobends:bends/animators/quadruped.json}. */
    public String animator;

    /** Render scale of a baby of this mob. */
    public float childScale = 0.5F;

    public List<BoneDefinition> bones = new ArrayList<>();

    /**
     * Optional: what the entity's data carries besides its bones, as name to kind
     * ({@code {"swordTrail": "mobends:sword_trail"}}); see {@code EntityComponents}.
     */
    public Map<String, String> components;

    /**
     * Optional: the renderer layers the model draws with, as id to options
     * ({@code {"mobends:custom_head": {"bone": "head"}}}); see {@code DefinedLayers}. A vanilla
     * layer no entry replaces is kept as it is.
     */
    public Map<String, JsonObject> layers;

    /**
     * Optional: a swing within this many ticks of the last counted one, while the main hand holds
     * an item, isn't counted (it continues a sword combo); 0 counts every swing.
     */
    public float attackComboTicks;

    /**
     * Optional: a bone's base smoothing (ticks to reach a target), what it gets in a frame where
     * nothing smooths it (otherwise 0, at once), by name: a bone, a component, or the entity's own
     * {@code root} and {@code center} (see {@link BoneSmoothing}).
     */
    public Map<String, BoneSmoothing> smoothing;

    /** Optional: where the renderer turns the entity's {@code center} bone about (see {@link CenterSettings}). */
    public CenterSettings center;

    public static class CenterSettings {
        /**
         * The pivot, in model units from the entity's feet (+Y up, +Z ahead of the body), scaled
         * with the model on a baby; null for half the entity's height.
         */
        public float[] pivot;
    }

    /**
     * Optional: how bones change on a baby, by name, besides the whole model's {@link #childScale}:
     * the scale of their offsets from their parents, and an offset before their parents' transforms
     * (a wolf pup's head).
     */
    public Map<String, ChildBone> childBones;

    public static class ChildBone {
        public float offsetScale = 1;
        public float[] globalOffset;
    }

    /** Optional: how the renderer places the model (see {@link RendererSettings}). */
    public RendererSettings renderer = new RendererSettings();

    public static class RendererSettings {
        /** How far up the model is drawn while the entity sneaks, in model units (vanilla lowers a sneaking player). */
        public float sneakOffset;
        /** The same while a player sneaks and flies (descends); null for {@link #sneakOffset}. */
        public Float flyingSneakOffset;
        /**
         * Whether the renderer turns the model back from the rock vanilla gives an iron golem while
         * it walks (RenderIronGolem), so the animator has the whole model to itself.
         */
        public boolean undoVanillaRock;
    }

    /** The entity scope's definitions, read by its animators as {@code entity.x}: the only place that reads the entity's fields. */
    @SerializedName("@define")
    public Map<String, DefinitionTemplate> define;
    /** The entity scope's statement lists. */
    @SerializedName("@on")
    public OnTemplate on;

    /**
     * Optional: the options the user sets in the Mo' Bends menu, read by the entity's animator as
     * {@code option.x} (see {@link OptionTemplate}).
     */
    public Map<String, OptionTemplate> options;

    /** Where it was loaded from, which owns its options (set when it is loaded; null when parsed from JSON alone). */
    @Nullable
    public transient String location;

    @Nullable
    private transient OptionSet optionSet;

    /** Whether the definition comes from a trusted source (set when it's loaded; see {@code PackTrust}). */
    public transient boolean trusted = true;

    /** The entity scope this definition declares for an entity of {@code type}. */
    public EntityTemplate entityScope(Class<?> type) {
        return new EntityTemplate(type, define, on, trusted, options());
    }

    /** The options it declares, owned by its location ({@code mobends:bends/models/player.json}). */
    public OptionSet options() {
        if (optionSet == null) {
            optionSet = KumoOptions.INSTANCE.declare(location != null ? location : String.valueOf(key), options);
        }
        return optionSet;
    }


    /** Every bone name, split segments included, in declaration order. */
    public List<String> allBoneNames() {
        List<String> names = new ArrayList<>();
        for (BoneDefinition bone : bones) {
            names.addAll(bone.segmentNames());
        }
        return names;
    }

    public BoneDefinition bone(String name) {
        for (BoneDefinition bone : bones) {
            if (name.equals(bone.name)) {
                return bone;
            }
        }
        return null;
    }


    /** Whether {@code name}, one of {@link #entity}, is a registry id ({@code minecraft:fox}) rather than a class name. */
    public static boolean isEntityId(String name) {
        return name.indexOf(':') >= 0;
    }

    /**
     * The key it has when it names none: from its first entity, as written, whether this version
     * knows it or not: an id's ({@code fox}, {@code mymod/my_mob}), or 1.12's id of a Minecraft
     * class ({@code zombie_pigman}), as 1.12 named it; null for another mod's class, which needs a
     * {@code key}.
     */
    @Nullable
    public String defaultKey() {
        String first = entity.get(0);
        String id = isEntityId(first) ? first : VanillaNames.legacyEntityId(first);
        ResourceLocation location = id == null ? null : ResourceLocation.tryParse(id);
        if (location == null) {
            return null;
        }
        return "minecraft".equals(location.getNamespace()) ? location.getPath() : location.getNamespace() + "/" + location.getPath();
    }

    public void validate() throws MalformedKumoTemplateException {
        if (entity == null || entity.isEmpty()) throw new MalformedKumoTemplateException("A model definition needs an 'entity': a registry id or a class name, or a list of them.");
        for (String name : entity) {
            if (name == null || name.isEmpty() || isEntityId(name) && ResourceLocation.tryParse(name) == null) {
                throw new MalformedKumoTemplateException("'" + name + "' is no entity id or class name.");
            }
        }
        if (animator == null) throw new MalformedKumoTemplateException("A model definition needs an 'animator'.");
        if (bones.isEmpty()) throw new MalformedKumoTemplateException("A model definition needs 'bones'.");
        OptionTemplate.validate(options, "the model definition");
        Set<String> seen = new HashSet<>();
        for (BoneDefinition bone : bones) {
            bone.validate();
            for (String name : bone.segmentNames()) {
                if (!seen.add(name)) {
                    throw new MalformedKumoTemplateException("Duplicate bone name '" + name + "'.");
                }
            }
            if (bone.overlay != null && (!seen.contains(bone.overlay) || bone(bone.overlay) == null)) {
                throw new MalformedKumoTemplateException("Bone '" + bone.name + "' lies over '" + bone.overlay + "', which is not a bone declared before it.");
            }
            if (bone.parent != null && !seen.contains(bone.parent)) {
                throw new MalformedKumoTemplateException("Bone '" + bone.name + "' names a parent '" + bone.parent + "' that is not declared before it.");
            }
        }
        for (String name : seen) {
            if (Skeleton.isEntityBone(name)) {
                throw new MalformedKumoTemplateException("A bone can't be named '" + name + "': it is one of the entity's own.");
            }
        }
        if (center != null && center.pivot != null && center.pivot.length != 3) {
            throw new MalformedKumoTemplateException("'center.pivot' is [x, y, z].");
        }
        if (renderer == null) {
            renderer = new RendererSettings();
        }
        if (childBones != null) {
            for (Map.Entry<String, ChildBone> entry : childBones.entrySet()) {
                if (!seen.contains(entry.getKey()))
                    throw new MalformedKumoTemplateException("'childBones' names '" + entry.getKey() + "', which is not a bone.");
                float[] offset = entry.getValue().globalOffset;
                if (offset != null && offset.length != 3)
                    throw new MalformedKumoTemplateException("'childBones." + entry.getKey() + ".globalOffset' is [x, y, z].");
            }
        }
        if (components != null) {
            for (String name : components.keySet()) {
                if (seen.contains(name) || Skeleton.isEntityBone(name)) {
                    throw new MalformedKumoTemplateException("The component '" + name + "' has the name of a bone.");
                }
            }
        }
    }

    /**
     * The model class {@link #model} names: Minecraft's by its 1.12 name, through
     * {@link VanillaNames#modelClass}; another mod's by its own full name (a {@code net.minecraft.}
     * name is never loaded that way: on Fabric, Minecraft's classes are renamed in the game). Null
     * if it names none, or a class not loaded here (a 1.12 model without a counterpart in this
     * version, or a mod that isn't installed).
     */
    @Nullable
    public Class<?> modelClass() {
        if (!modelClassFound) {
            modelClassFound = true;
            if (model != null) {
                modelClass = VanillaNames.modelClass(model);
                if (modelClass == null && !model.startsWith("net.minecraft.")) {
                    try {
                        // Without initialising it: a resource pack may name any class.
                        modelClass = Class.forName(model, false, EntityModelDefinition.class.getClassLoader());
                    } catch (ClassNotFoundException | LinkageError e) {
                        modelClass = null;
                    }
                }
            }
        }
        return modelClass;
    }

    /** Whether {@link #model} names a vanilla model, whose bones' fields are 1.12's names ({@link VanillaNames#modelPart}). */
    public boolean namesVanillaModel() {
        return model != null && VanillaNames.modelClass(model) != null;
    }
}
