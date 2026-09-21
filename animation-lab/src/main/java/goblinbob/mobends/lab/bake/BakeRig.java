package goblinbob.mobends.lab.bake;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.SmoothVector3f;
import goblinbob.mobends.core.math.vector.Vec3f;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.LabBootstrap;
import goblinbob.mobends.lab.trace.PoseCapture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Drives a single procedural animation bit under fully scripted inputs and reads back the
 * targets it writes. The bones are reset to identity before every sample, so what comes out is
 * the pose the bit *asks for*, independent of any smoothing history.
 */
public class BakeRig
{
    public final LivingEntityData<?> data;
    private final Map<String, Object> parts;

    public BakeRig(EntityKind kind)
    {
        LabBootstrap.ensure();
        net.minecraft.entity.Entity.resetIds();
        World world = new World();
        Minecraft.getMinecraft().world = world;
        Minecraft.getMinecraft().player = new EntityPlayerSP(world);
        EntityLivingBase entity = kind.createEntity(world);
        entity.setLocationAndAngles(0.5, 0, 0.5, 0, 0);
        this.data = kind.createData(entity, 1L);
        this.parts = PoseCapture.partsOf(data);
        DataUpdateHandler.reset();
        DataUpdateHandler.ticksPerFrame = 1F / 3F;
    }

    public EntityLivingBase entity()
    {
        return data.getEntity();
    }

    /** Resets every rotation to identity and every offset/vector to zero. */
    public void resetPose()
    {
        for (Object part : parts.values())
        {
            if (part instanceof IModelPart)
            {
                ((IModelPart) part).getRotation().identity();
                ((IModelPart) part).getOffset().set(0, 0, 0);
            }
            else if (part instanceof SmoothOrientation)
            {
                ((SmoothOrientation) part).identity();
            }
            else if (part instanceof SmoothVector3f)
            {
                ((SmoothVector3f) part).set(0, 0, 0);
            }
        }
        data.globalOffset.set(0, 0, 0);
        data.localOffset.set(0, 0, 0);
    }

    /** A rotation no animation ever produces, used to detect which bones an onPlay touched. */
    public static final Quaternion MARKER = markerQuaternion();

    private static Quaternion markerQuaternion()
    {
        Quaternion q = new Quaternion();
        q.setFromAxisAngle(0.267261F, 0.534522F, 0.801784F, 1.234567F);
        return q;
    }

    /** Sets every rotation to the marker; bones that still hold it afterwards were not touched. */
    public void resetPoseToMarker()
    {
        resetPose();
        for (Object part : parts.values())
        {
            SmoothOrientation orientation = part instanceof IModelPart ? ((IModelPart) part).getRotation()
                    : part instanceof SmoothOrientation ? (SmoothOrientation) part : null;
            if (orientation != null)
            {
                orientation.snapTo(MARKER);
            }
        }
    }

    /** A vector value no animation ever produces (see {@link #MARKER}). */
    public static final Vec3f VECTOR_MARKER = new Vec3f(1234.5F, -2345.6F, 3456.7F);

    /** Sets every vector target to the marker so untouched vectors can be told apart. */
    public void resetVectorsToMarker()
    {
        data.globalOffset.set(VECTOR_MARKER.x, VECTOR_MARKER.y, VECTOR_MARKER.z);
        data.localOffset.set(VECTOR_MARKER.x, VECTOR_MARKER.y, VECTOR_MARKER.z);
        for (Object part : parts.values())
        {
            if (part instanceof SmoothVector3f)
            {
                ((SmoothVector3f) part).set(VECTOR_MARKER.x, VECTOR_MARKER.y, VECTOR_MARKER.z);
            }
        }
    }

    /** Which axes of a vector still hold the marker (true = untouched). */
    public static boolean[] markerAxes(Vec3f v)
    {
        return new boolean[] {
                Math.abs(v.x - VECTOR_MARKER.x) < 1e-3F,
                Math.abs(v.y - VECTOR_MARKER.y) < 1e-3F,
                Math.abs(v.z - VECTOR_MARKER.z) < 1e-3F };
    }

    public static boolean isMarker(Quaternion q)
    {
        return Math.abs(q.x - MARKER.x) < 1e-6 && Math.abs(q.y - MARKER.y) < 1e-6 && Math.abs(q.z - MARKER.z) < 1e-6 && Math.abs(q.w - MARKER.w) < 1e-6;
    }

    public void limbSwing(float value) { data.limbSwing.set(value); }
    public void limbSwingAmount(float value) { data.limbSwingAmount.set(value); }
    public void headYaw(float value) { data.headYaw.set(value); }
    public void headPitch(float value) { data.headPitch.set(value); }
    public void ticks(float value) { DataUpdateHandler.setTicks(value); }

    /** Sets a protected counter of the data class by name (e.g. "ticksAfterTouchdown"). */
    public void set(String field, float value)
    {
        Class<?> c = data.getClass();
        while (c != null)
        {
            try
            {
                Field f = c.getDeclaredField(field);
                f.setAccessible(true);
                if (f.getType() == float.class) f.setFloat(data, value);
                else if (f.getType() == int.class) f.setInt(data, (int) value);
                else if (f.getType() == double.class) f.setDouble(data, value);
                else if (f.getType() == boolean.class) f.setBoolean(data, value != 0);
                else throw new IllegalArgumentException("unsupported field type " + f.getType());
                return;
            }
            catch (NoSuchFieldException e)
            {
                c = c.getSuperclass();
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalArgumentException("no field " + field + " on " + data.getClass());
    }

    @SuppressWarnings("unchecked")
    public void perform(AnimationBit<?> bit)
    {
        ((AnimationBit<LivingEntityData<?>>) bit).perform(data);
    }

    @SuppressWarnings("unchecked")
    public void play(AnimationBit<?> bit)
    {
        ((AnimationBit<LivingEntityData<?>>) bit).onPlay(data);
    }

    /** The rotation targets of every named rotation part. */
    public Map<String, Quaternion> rotationTargets()
    {
        Map<String, Quaternion> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : parts.entrySet())
        {
            Object part = entry.getValue();
            if (part instanceof IModelPart)
            {
                result.put(entry.getKey(), copy(((IModelPart) part).getRotation().getEnd()));
            }
            else if (part instanceof SmoothOrientation)
            {
                result.put(entry.getKey(), copy(((SmoothOrientation) part).getEnd()));
            }
        }
        return result;
    }

    /** The current smoothed rotations (after {@code orientInstant}, these equal the targets). */
    public Map<String, Quaternion> rotationSmooth()
    {
        Map<String, Quaternion> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : parts.entrySet())
        {
            Object part = entry.getValue();
            if (part instanceof IModelPart)
            {
                result.put(entry.getKey(), copy(((IModelPart) part).getRotation().getSmooth()));
            }
            else if (part instanceof SmoothOrientation)
            {
                result.put(entry.getKey(), copy(((SmoothOrientation) part).getSmooth()));
            }
        }
        return result;
    }

    /** Vector targets: the entity offsets plus any named SmoothVector3f parts. */
    public Map<String, Vec3f> vectorTargets()
    {
        Map<String, Vec3f> result = new LinkedHashMap<>();
        result.put("root", new Vec3f(data.globalOffset.end));
        result.put("localOffset", new Vec3f(data.localOffset.end));
        for (Map.Entry<String, Object> entry : parts.entrySet())
        {
            if (entry.getValue() instanceof SmoothVector3f)
            {
                result.put(entry.getKey(), new Vec3f(((SmoothVector3f) entry.getValue()).end));
            }
        }
        return result;
    }

    /** The smoothness each rotation part currently has (what the bit's setSmoothness calls left). */
    public Map<String, Float> smoothness()
    {
        Map<String, Float> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : parts.entrySet())
        {
            Object part = entry.getValue();
            SmoothOrientation orientation = part instanceof IModelPart ? ((IModelPart) part).getRotation()
                    : part instanceof SmoothOrientation ? (SmoothOrientation) part : null;
            if (orientation != null)
            {
                result.put(entry.getKey(), smoothnessOf(orientation));
            }
        }
        result.put("root", data.globalOffset.smoothness.y);
        result.put("localOffset", data.localOffset.smoothness.y);
        return result;
    }

    private static Field SMOOTHNESS;

    public static float smoothnessOf(SmoothOrientation orientation)
    {
        try
        {
            if (SMOOTHNESS == null)
            {
                SMOOTHNESS = SmoothOrientation.class.getDeclaredField("smoothness");
                SMOOTHNESS.setAccessible(true);
            }
            return SMOOTHNESS.getFloat(orientation);
        }
        catch (ReflectiveOperationException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public static Quaternion copy(Quaternion q)
    {
        return new Quaternion(q.x, q.y, q.z, q.w);
    }
}
