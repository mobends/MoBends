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
     * an invisible stand-in, which passes the vanilla model's visibility and {@code postRender} on
     * to the bone) and its {@code position} is relative to the parent.
     */
    public String parent;

    /**
     * Where the bone sits, in model units: relative to its parent if it has one. Default: where its
     * {@link #pivot} (or else its vanilla part's rotation point) is, relative to the parent's.
     * Moves the boxes with it.
     */
    public float[] position;

    /**
     * Where the bone turns, in model units (as a vanilla rotation point is), when it isn't where
     * the vanilla part turns: the boxes stay where they are (a body that bends at the hips, not
     * the neck). What vanilla attaches to the part ({@code postRender}: a held item, a hat) stays
     * where vanilla puts it.
     */
    public float[] pivot;

    /**
     * Another bone this one is laid over (a sleeve over an arm): it turns with that bone, at its
     * pivot, and its boxes are split as that bone's are, each piece following that bone's segment.
     */
    public String overlay;

    /**
     * Extra inflation of the boxes, per segment ([x, y, z] each, the bone's own first), on top of
     * the vanilla one: a hair, so faces that meet don't flicker.
     */
    public float[][] inflate;

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
        if (pivot != null && pivot.length != 3) throw new MalformedKumoTemplateException("Bone '" + name + "': 'pivot' needs three components.");
        if (overlay != null && (parent != null || position != null || pivot != null || split != null))
            throw new MalformedKumoTemplateException("Bone '" + name + "': an 'overlay' takes the bone it lies over's place and segments: it has no 'parent', 'position', 'pivot' or 'split' of its own.");
        if (inflate != null)
        {
            for (float[] segment : inflate)
            {
                if (segment == null || segment.length != 3) throw new MalformedKumoTemplateException("Bone '" + name + "': 'inflate' is a list of [x, y, z], one per segment.");
            }
        }
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
            if (split.axis == null || split.axisIndex() < 0)
                throw new MalformedKumoTemplateException("Bone '" + name + "': split 'axis' must be x, y or z, not '" + split.axis + "'.");
            if (split.hinge != null && split.hingeAxis() < 0 && !"center".equals(split.hinge))
                throw new MalformedKumoTemplateException("Bone '" + name + "': split 'hinge' must be front, back, top, bottom or center, not '" + split.hinge + "'.");
            if (split.hingeAxis() == split.axisIndex())
                throw new MalformedKumoTemplateException("Bone '" + name + "': a split along " + split.axis + " can't hinge at its " + split.hinge + ", which is on the same axis.");
        }
        if (vanilla != null && vanilla.field == null && vanilla.index < 0)
            throw new MalformedKumoTemplateException("Bone '" + name + "': 'vanilla' needs a 'field' name and/or a box-list 'index'.");
    }

    public static class VanillaPart
    {
        /** The model's field holding the part, by its development name (see {@link DefinedFields}). */
        public String field;
        /** For an array field: the element. */
        public int element = -1;
        /** Optional fallback: the part's position in the model's box list (creation order). */
        public int index = -1;
    }

    public static class SplitDefinition
    {
        public String axis = "y";
        /** Cut positions as fractions of the box length along the axis, increasing. */
        public float[] at;
        /** Names of the segments after the first, one per cut. */
        public List<String> names;
        /**
         * Where on the cut each joint sits: {@code front} / {@code back} (the -Z / +Z edge),
         * {@code top} / {@code bottom} (the -Y / +Y edge), or {@code center} (default). A joint that
         * bends one way hinges at the edge on the other side (a knee at the front), so the segments
         * stay joined there.
         */
        public String hinge;
        /**
         * Whether the faces at the cuts are drawn, closing each segment, so a bent joint shows no
         * gap (see {@link BoxSplitter#split}). Default: they are hidden.
         */
        public boolean caps;

        public int axisIndex()
        {
            return axis.length() == 1 ? "xyz".indexOf(axis) : -1;
        }

        /** The axis the hinge edge is on (1 = Y, 2 = Z), or -1 for the center. */
        public int hingeAxis()
        {
            String h = hinge == null ? "center" : hinge;
            return h.equals("front") || h.equals("back") ? 2 : h.equals("top") || h.equals("bottom") ? 1 : -1;
        }

        /** The hinge's edge on {@link #hingeAxis()}: -1 for the low one (front, top), 1 for the high one. */
        public int hingeSide()
        {
            String h = hinge == null ? "center" : hinge;
            return h.equals("front") || h.equals("top") ? -1 : h.equals("back") || h.equals("bottom") ? 1 : 0;
        }
    }

}
