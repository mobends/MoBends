package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.bind.IBoneSink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The set of bone names an animator touches, resolved once into indices, and bound to a
 * subject's sinks. Templates refer to bones by index after instancing, so the per-frame path
 * does no string lookups.
 */
public class Skeleton
{

    /** Name of the slot that maps to the subject's global offset in legacy clips. */
    public static final String ROOT = "root";

    /** Bone names that denote entity-level smoothed vectors rather than rotations. */
    public static boolean isVectorBone(String name)
    {
        return ROOT.equals(name) || "globalOffset".equals(name) || "localOffset".equals(name);
    }

    private final List<String> names = new ArrayList<>();
    private final Map<String, Integer> indices = new HashMap<>();
    private IBoneSink[] sinks = new IBoneSink[0];
    private IKumoSubject boundTo;

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

    public void bind(IKumoSubject subject)
    {
        if (boundTo == subject && sinks.length == names.size())
        {
            return;
        }
        sinks = new IBoneSink[names.size()];
        for (int i = 0; i < sinks.length; i++)
        {
            sinks[i] = subject.getBone(names.get(i));
        }
        boundTo = subject;
    }

    public IBoneSink sink(int index)
    {
        return index < sinks.length ? sinks[index] : null;
    }

    public boolean isBound()
    {
        return boundTo != null;
    }

}
