package goblinbob.mobends.lab.bake;

import com.google.gson.GsonBuilder;
import goblinbob.mobends.core.animation.keyframe.Bone;
import goblinbob.mobends.core.animation.keyframe.Keyframe;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Builds a format-2 keyframe clip from sampled poses and writes it as JSON. Positions of ordinary
 * bones are stored negated, matching the mod's convention that a clip position is applied as
 * {@code offset = -position}; the "root" bone's position is the global offset as is.
 */
public class ClipBuilder
{
    private final LinkedHashMap<String, List<Keyframe>> bones = new LinkedHashMap<>();
    private Float duration;
    private Boolean loop;
    private String interpolation;
    private int frames = 0;
    private final List<Float> times = new ArrayList<>();

    public ClipBuilder duration(float duration) { this.duration = duration; return this; }
    public ClipBuilder loop(boolean loop) { this.loop = loop; return this; }
    public ClipBuilder step() { this.interpolation = "STEP"; return this; }

    /** Appends one keyframe at an explicit time. */
    public ClipBuilder frame(float time, Map<String, Quaternion> rotations, Map<String, Vec3f> vectors)
    {
        times.add(time);
        return frame(rotations, vectors);
    }

    /** Appends one keyframe for every rotation part and vector given. */
    public ClipBuilder frame(Map<String, Quaternion> rotations, Map<String, Vec3f> vectors)
    {
        for (Map.Entry<String, Quaternion> entry : rotations.entrySet())
        {
            Keyframe k = new Keyframe();
            Quaternion q = entry.getValue();
            k.rotation = new float[] { q.x, q.y, q.z, q.w };
            k.position = new float[] { 0, 0, 0 };
            k.scale = new float[] { 1, 1, 1 };
            bones.computeIfAbsent(entry.getKey(), n -> new ArrayList<>()).add(k);
        }
        if (vectors != null)
        {
            for (Map.Entry<String, Vec3f> entry : vectors.entrySet())
            {
                Keyframe k = new Keyframe();
                Vec3f v = entry.getValue();
                k.rotation = new float[] { 0, 0, 0, 1 };
                k.position = new float[] { v.x, v.y, v.z };
                k.scale = new float[] { 1, 1, 1 };
                bones.computeIfAbsent(entry.getKey(), n -> new ArrayList<>()).add(k);
            }
        }
        frames++;
        return this;
    }

    /** Drops bones that stay at identity / zero for the whole clip. */
    public ClipBuilder pruneIdentity()
    {
        bones.entrySet().removeIf(entry -> {
            for (Keyframe k : entry.getValue())
            {
                if (!isIdentity(k)) return false;
            }
            return true;
        });
        return this;
    }

    /** Keeps only the given bones. */
    public ClipBuilder only(String... names)
    {
        Set<String> keep = new HashSet<>(Arrays.asList(names));
        bones.keySet().retainAll(keep);
        return this;
    }

    public ClipBuilder without(String... names)
    {
        for (String name : names) bones.remove(name);
        return this;
    }

    public boolean isEmpty()
    {
        return bones.isEmpty();
    }

    public Set<String> boneNames()
    {
        return bones.keySet();
    }

    private static boolean isIdentity(Keyframe k)
    {
        float[] r = k.rotation;
        float[] p = k.position;
        return Math.abs(r[0]) < 1e-7 && Math.abs(r[1]) < 1e-7 && Math.abs(r[2]) < 1e-7 && Math.abs(Math.abs(r[3]) - 1) < 1e-7
                && Math.abs(p[0]) < 1e-7 && Math.abs(p[1]) < 1e-7 && Math.abs(p[2]) < 1e-7;
    }

    public KeyframeAnimation build()
    {
        KeyframeAnimation animation = new KeyframeAnimation();
        animation.bones = new LinkedHashMap<>();
        for (Map.Entry<String, List<Keyframe>> entry : bones.entrySet())
        {
            Bone bone = new Bone();
            bone.keyframes = entry.getValue();
            animation.bones.put(entry.getKey(), bone);
        }
        animation.duration = duration;
        animation.loop = loop;
        animation.interpolation = interpolation;
        if (!times.isEmpty() && times.size() == frames)
        {
            animation.times = new float[times.size()];
            for (int i = 0; i < animation.times.length; i++) animation.times[i] = times.get(i);
        }
        return animation;
    }

    public void write(Path file) throws IOException
    {
        Files.createDirectories(file.getParent());
        KeyframeAnimation animation = build();
        com.google.gson.Gson gson = new GsonBuilder()
                .registerTypeAdapter(float[].class, new com.google.gson.TypeAdapter<float[]>()
                {
                    @Override
                    public void write(com.google.gson.stream.JsonWriter out, float[] value) throws IOException
                    {
                        if (value == null)
                        {
                            out.nullValue();
                            return;
                        }
                        out.beginArray();
                        for (float f : value)
                        {
                            out.value(BigDecimal.valueOf(f).setScale(7, RoundingMode.HALF_EVEN).stripTrailingZeros());
                        }
                        out.endArray();
                    }

                    @Override
                    public float[] read(com.google.gson.stream.JsonReader in)
                    {
                        throw new UnsupportedOperationException();
                    }
                })
                .create();
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8))
        {
            gson.toJson(animation, writer);
        }
    }
}
