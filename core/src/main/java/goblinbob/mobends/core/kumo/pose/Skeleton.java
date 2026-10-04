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

    // The entity-level bones of the format (see misc/kumo-format.md, "Bones").

    /** The subject's offset from its position; what clips call {@link #ROOT}. */
    public static final String GLOBAL_OFFSET = "globalOffset";
    /** The clips' name of {@link #GLOBAL_OFFSET}: its keyframe positions are root motion. */
    public static final String ROOT = "root";
    /** The subject's offset after its rotation. */
    public static final String LOCAL_OFFSET = "localOffset";
    /** Rotates the whole subject about its center; its keyframe positions are ignored. */
    public static final String CENTER_ROTATION = "centerRotation";

    /** Bone names that denote entity-level smoothed vectors rather than rotations. */
    public static boolean isVectorBone(String name)
    {
        return ROOT.equals(name) || GLOBAL_OFFSET.equals(name) || LOCAL_OFFSET.equals(name);
    }

    private final List<String> names = new ArrayList<>();
    private final Map<String, Integer> indices = new HashMap<>();

    public int indexOf(String bone)
    {
        Integer index = indices.get(bone);
        if (index == null)
        {
            index = names.size();
            names.add(bone);
            indices.put(bone, index);
        }
        return index;
    }

    public int size()
    {
        return names.size();
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
