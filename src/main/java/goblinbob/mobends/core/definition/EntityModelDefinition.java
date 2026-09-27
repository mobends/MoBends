package goblinbob.mobends.core.definition;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A mob described as data (`assets/mobends/bends/models/<mob>.json`): which entity, which vanilla
 * model parts become bones (optionally split into more segments for extra joints), which animator
 * drives them, and which entity fields the animator can read. The generic data class, mutator and
 * renderer are built from it, so a mob that never had a Mo' Bends treatment needs only this file
 * and an animator.
 */
public class EntityModelDefinition
{

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

    /** Entity fields exposed as animator variables. */
    public List<VariableDefinition> variables = new ArrayList<>();

    /** Bones bends packs may alter; default: every bone. */
    public List<String> alterableParts;

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

    public String[] alterablePartsOrAll()
    {
        List<String> parts = alterableParts != null ? alterableParts : allBoneNames();
        return parts.toArray(new String[0]);
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
            if (bone.parent != null && !seen.contains(bone.parent))
            {
                throw new MalformedKumoTemplateException("Bone '" + bone.name + "' names a parent '" + bone.parent + "' that is not declared before it.");
            }
        }
        for (VariableDefinition variable : variables)
        {
            variable.validate();
        }
    }

}
