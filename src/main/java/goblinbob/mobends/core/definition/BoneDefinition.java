package goblinbob.mobends.core.definition;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.ArrayList;
import java.util.List;

/** One bone of an {@link EntityModelDefinition}. */
public class BoneDefinition
{

    public String name;

    /**
     * The vanilla model part this bone takes over: its boxes, texture and rotation point. Null for
     * a purely virtual bone (a transform with no geometry of its own).
     */
    public VanillaPart vanilla;

    /**
     * Optional parent bone. A bone with a parent renders inside it (its vanilla part is replaced by
     * an invisible stand-in) and its {@code position} is relative to the parent.
     */
    public String parent;

    /** Pivot in model units; default: the vanilla part's rotation point. */
    public float[] position;

    /**
     * Constant rotation (degrees X, Y, Z) applied before the animated one, for parts the vanilla
     * model holds at a fixed angle (a quadruped's body lies along Z: {@code [90, 0, 0]}).
     */
    public float[] restRotation;

    /** Cut the vanilla boxes into segments, each a further bone (a knee, an elbow, tentacle joints). */
    public SplitDefinition split;

    /** The bone's own name, then the names of the extra segments. */
    public List<String> segmentNames()
    {
        List<String> names = new ArrayList<>();
        names.add(name);
        if (split != null && split.names != null)
        {
            names.addAll(split.names);
        }
        return names;
    }

    public void validate() throws MalformedKumoTemplateException
    {
        if (name == null) throw new MalformedKumoTemplateException("A bone needs a 'name'.");
        if (position != null && position.length != 3) throw new MalformedKumoTemplateException("Bone '" + name + "': 'position' needs three components.");
        if (restRotation != null && restRotation.length != 3) throw new MalformedKumoTemplateException("Bone '" + name + "': 'restRotation' needs three angles.");
        if (split != null)
        {
            if (split.at == null || split.at.length == 0) throw new MalformedKumoTemplateException("Bone '" + name + "': 'split' needs 'at' fractions.");
            if (split.names == null || split.names.size() != split.at.length)
                throw new MalformedKumoTemplateException("Bone '" + name + "': 'split' needs one name per cut.");
            float last = 0;
            for (float fraction : split.at)
            {
                if (fraction <= last || fraction >= 1) throw new MalformedKumoTemplateException("Bone '" + name + "': split fractions must increase within (0, 1).");
                last = fraction;
            }
            if (split.axis == null || "XYZ".indexOf(split.axis.toUpperCase()) < 0 || split.axis.length() != 1)
                throw new MalformedKumoTemplateException("Bone '" + name + "': split 'axis' must be X, Y or Z.");
        }
        if (vanilla != null && vanilla.field == null && vanilla.index < 0)
            throw new MalformedKumoTemplateException("Bone '" + name + "': 'vanilla' needs a 'field' name and/or a box-list 'index'.");
    }

    public static class VanillaPart
    {
        /** The model's field holding the part (the deobfuscated name; used when it resolves). */
        public String field;
        /** For an array field: the element. */
        public int element = -1;
        /** Fallback: the part's position in the model's box list (creation order). */
        public int index = -1;
    }

    public static class SplitDefinition
    {
        public String axis = "Y";
        /** Cut positions as fractions of the box length along the axis, increasing. */
        public float[] at;
        /** Names of the segments after the first, one per cut. */
        public List<String> names;

        public int axisIndex()
        {
            return "XYZ".indexOf(axis.toUpperCase());
        }
    }

}
