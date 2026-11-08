package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.bind.IBoneSink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The set of bone names an animator touches, resolved once into indices; an entity's state holds
 * its sinks for them. Templates refer to bones by index after instancing, so the per-frame path
 * does no string lookups.
 */
public class Skeleton
{

    // The entity's own bones (see misc/kumo-format.md, "Bones"): the model hangs from them.

    /**
     * Moves and turns the whole model about its feet: its keyframe positions are root motion. The
     * parent of {@link #CENTER}.
     */
    public static final String ROOT = "root";
    /** Moves the whole model in {@link #ROOT}'s frame, then turns it about its center (a pivot, half the entity's height by default). */
    public static final String CENTER = "center";

    /**
     * Whether the bone is one of the entity's own ({@link #ROOT}, {@link #CENTER}): its position is
     * a smoothed vector, not a part's offset, and it turns as well.
     */
    public static boolean isEntityBone(String name)
    {
        return ROOT.equals(name) || CENTER.equals(name);
    }

    private final List<String> names = new ArrayList<>();
    private final List<Boolean> entityBones = new ArrayList<>();
    private final Map<String, Integer> indices = new HashMap<>();

    public int indexOf(String bone)
    {
        Integer index = indices.get(bone);
        if (index == null)
        {
            index = names.size();
            names.add(bone);
            entityBones.add(isEntityBone(bone));
            indices.put(bone, index);
        }
        return index;
    }

    public int size()
    {
        return names.size();
    }

    /** As {@link #isEntityBone(String)}, for a slot. */
    public boolean isEntityBone(int index)
    {
        return entityBones.get(index);
    }

    public String nameOf(int index)
    {
        return names.get(index);
    }

    /** The subject's sinks for every bone, by index (null where it has no such bone). */
    public IBoneSink[] sinksOf(IKumoSubject subject)
    {
        IBoneSink[] sinks = new IBoneSink[names.size()];
        for (int i = 0; i < sinks.length; i++)
        {
            sinks[i] = subject.getBone(names.get(i));
        }
        return sinks;
    }

}
