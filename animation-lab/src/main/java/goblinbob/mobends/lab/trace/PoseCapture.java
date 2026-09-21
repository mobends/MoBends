package goblinbob.mobends.lab.trace;

import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.IVec3fRead;
import goblinbob.mobends.core.math.vector.SmoothVector3f;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reads every named part of an {@link EntityData} into a {@link FramePose}. Parts are discovered
 * through the data's name-to-part map, so the capture works for any entity without a bone list.
 */
public class PoseCapture
{
    private static final Field NAME_TO_PART_MAP;

    static
    {
        try
        {
            NAME_TO_PART_MAP = EntityData.class.getDeclaredField("nameToPartMap");
            NAME_TO_PART_MAP.setAccessible(true);
        }
        catch (NoSuchFieldException e)
        {
            throw new IllegalStateException("EntityData no longer has a nameToPartMap field", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> partsOf(EntityData<?> data)
    {
        try
        {
            // Sorted for stable JSON output.
            return new TreeMap<>((Map<String, Object>) NAME_TO_PART_MAP.get(data));
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public static FramePose capture(EntityData<?> data)
    {
        FramePose frame = new FramePose();

        for (Map.Entry<String, Object> entry : partsOf(data).entrySet())
        {
            Object part = entry.getValue();
            if (part instanceof IModelPart)
            {
                IModelPart modelPart = (IModelPart) part;
                BonePose pose = new BonePose();
                pose.r = quat(modelPart.getRotation().getSmooth());
                pose.rt = quat(modelPart.getRotation().getEnd());
                pose.o = vec(modelPart.getOffset());
                pose.p = vec(modelPart.getPosition());
                pose.g = vec(modelPart.getGlobalOffset());
                pose.s = vec(modelPart.getScale());
                frame.bones.put(entry.getKey(), pose);
            }
            else if (part instanceof SmoothOrientation)
            {
                SmoothOrientation orientation = (SmoothOrientation) part;
                BonePose pose = new BonePose();
                pose.r = quat(orientation.getSmooth());
                pose.rt = quat(orientation.getEnd());
                frame.bones.put(entry.getKey(), pose);
            }
            else if (part instanceof SmoothVector3f)
            {
                frame.vectors.put(entry.getKey(), smoothVec((SmoothVector3f) part));
            }
        }

        frame.vectors.put("globalOffset", smoothVec(data.globalOffset));
        frame.vectors.put("localOffset", smoothVec(data.localOffset));
        return frame;
    }

    public static float[] quat(Quaternion q)
    {
        return new float[] { q.x, q.y, q.z, q.w };
    }

    public static float[] vec(IVec3fRead v)
    {
        return new float[] { v.getX(), v.getY(), v.getZ() };
    }

    public static VectorPose smoothVec(SmoothVector3f v)
    {
        VectorPose pose = new VectorPose();
        pose.v = new float[] { v.getX(), v.getY(), v.getZ() };
        pose.vt = new float[] { v.end.x, v.end.y, v.end.z };
        return pose;
    }
}
