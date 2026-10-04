package goblinbob.mobends.core.definition;

import com.google.gson.annotations.SerializedName;
import goblinbob.mobends.core.kumo.state.template.DefinitionTemplate;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.OnTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A mob described as data (`assets/mobends/bends/models/<mob>.json`): which entity, which vanilla
 * model parts become bones (optionally split into more segments for extra joints), which animator
 * drives them, and the entity scope its animators read ({@code entity.x}). The generic data class, mutator and
 * renderer are built from it, so a mob that never had a Mo' Bends treatment needs only this file
 * and an animator.
 */
public class EntityModelDefinition
{

    /** The {@code formatVersion} a model definition has to have (see {@link goblinbob.mobends.core.util.FormatVersion}). */
    public static final int FORMAT_VERSION = 2;

    /** Fully qualified entity class, e.g. {@code net.minecraft.entity.passive.EntityCow}. */
    public String entity;

    /** Optional: only models of this class (or a subclass) are mutated. */
    public String model;

    /** Optional custom bender key / language key; defaults come from the entity registry. */
    public String key;
    public String unlocalizedName;

    /** The animator asset, e.g. {@code mobends:bends/animators/quadruped.json}. */
    public String animator;

    /** Render scale of a baby of this mob. */
    public float childScale = 0.5F;

    public List<BoneDefinition> bones = new ArrayList<>();

    /** The entity scope's definitions, read by its animators as {@code entity.x}: the only place that reads the entity's fields. */
    @SerializedName("@define")
    public Map<String, DefinitionTemplate> define;
    /** The entity scope's statement lists. */
    @SerializedName("@on")
    public OnTemplate on;

    /** Whether the definition comes from a trusted source (set when it is loaded). */
    public transient boolean trusted = true;

    /** The entity scope this definition declares for an entity of {@code type}. */
    public EntityTemplate entityScope(Class<?> type)
    {
        return new EntityTemplate(type, define, on, trusted);
    }


    /** Every bone name, split segments included, in declaration order. */
    public List<String> allBoneNames()
    {
        List<String> names = new ArrayList<>();
        for (BoneDefinition bone : bones)
        {
            names.addAll(bone.segmentNames());
        }
        return names;
    }

    public BoneDefinition bone(String name)
    {
        for (BoneDefinition bone : bones)
        {
            if (name.equals(bone.name))
            {
                return bone;
            }
        }
        return null;
    }


    public void validate() throws MalformedKumoTemplateException
    {
        if (entity == null) throw new MalformedKumoTemplateException("A model definition needs an 'entity' class.");
        if (animator == null) throw new MalformedKumoTemplateException("A model definition needs an 'animator'.");
        if (bones.isEmpty()) throw new MalformedKumoTemplateException("A model definition needs 'bones'.");
        Set<String> seen = new HashSet<>();
        for (BoneDefinition bone : bones)
        {
            bone.validate();
            for (String name : bone.segmentNames())
            {
                if (!seen.add(name))
                {
                    throw new MalformedKumoTemplateException("Duplicate bone name '" + name + "'.");
                }
            }
            if (bone.overlay != null && (!seen.contains(bone.overlay) || bone(bone.overlay) == null))
            {
                throw new MalformedKumoTemplateException("Bone '" + bone.name + "' lies over '" + bone.overlay + "', which is not a bone declared before it.");
            }
            if (bone.parent != null && !seen.contains(bone.parent))
            {
                throw new MalformedKumoTemplateException("Bone '" + bone.name + "' names a parent '" + bone.parent + "' that is not declared before it.");
            }
        }
    }

}
