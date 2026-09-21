package goblinbob.mobends.lab.bake;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.kumo.pose.PoseMath;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;
import goblinbob.mobends.lab.compare.PoseComparator;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Sampling recipes on top of {@link BakeRig}.
 */
public class Baker
{
    /** A scenario for one sample: sets the rig's inputs for parameter value {@code t}. */
    @FunctionalInterface
    public interface Setup
    {
        void apply(BakeRig rig, float t);
    }

    private final BakeRig rig;
    public final StringBuilder log = new StringBuilder();
    /** Vectors a bit wrote only some axes of (e.g. slideY); the animator has to keep the other axes' damping. */
    public final java.util.Set<String> partialVectorWrites = new java.util.TreeSet<>();

    public Baker(BakeRig rig)
    {
        this.rig = rig;
    }

    /** Samples the bit's targets at one parameter value. */
    public Sample sample(AnimationBit<?> bit, Setup setup, float t)
    {
        rig.resetPose();
        setup.apply(rig, t);
        rig.perform(bit);
        Sample sample = new Sample(rig.rotationTargets(), rig.vectorTargets(), rig.smoothness());
        // Keep only what the bit wrote: run it once more on marker values and drop the untouched ones.
        rig.resetPoseToMarker();
        rig.resetVectorsToMarker();
        setup.apply(rig, t);
        rig.perform(bit);
        Map<String, Quaternion> after = rig.rotationTargets();
        sample.rotations.keySet().removeIf(bone -> BakeRig.isMarker(after.get(bone)));
        Map<String, Vec3f> vectorsAfter = rig.vectorTargets();
        sample.vectors.entrySet().removeIf(entry -> {
            boolean[] untouched = BakeRig.markerAxes(vectorsAfter.get(entry.getKey()));
            if (untouched[0] && untouched[1] && untouched[2]) return true;
            if (untouched[0] || untouched[1] || untouched[2])
            {
                partialVectorWrites.add(entry.getKey() + (untouched[0] ? "" : "x") + (untouched[1] ? "" : "y") + (untouched[2] ? "" : "z"));
            }
            return false;
        });
        return sample;
    }

    /**
     * Samples a cycle with uniform keyframes, then locates every discontinuity (an angular jump
     * above {@code jumpDeg} between neighbours) by bisection and inserts a keyframe pair around
     * it, so stepped and sawtooth targets are reproduced instead of ramped.
     */
    public ClipBuilder adaptiveCycle(AnimationBit<?> bit, Setup setup, float period, int count, double jumpDeg)
    {
        java.util.TreeMap<Float, Sample> samples = new java.util.TreeMap<>();
        for (int i = 0; i <= count; i++)
        {
            float t = period * i / count;
            samples.put(t, sample(bit, setup, i == count ? 0 : t));
        }

        java.util.List<Float> keys = new java.util.ArrayList<>(samples.keySet());
        int inserted = 0;
        for (int i = 0; i + 1 < keys.size(); i++)
        {
            float a = keys.get(i), b = keys.get(i + 1);
            if (maxJump(samples.get(a), samples.get(b)) <= jumpDeg) continue;
            // Bisect until the jump is confined to a tiny interval, then keep both sides.
            Sample sa = samples.get(a), sb = samples.get(b);
            for (int step = 0; step < 24 && (b - a) > 1e-6F; step++)
            {
                float m = (a + b) / 2;
                Sample sm = sample(bit, setup, m);
                if (maxJump(sa, sm) > jumpDeg) { b = m; sb = sm; } else { a = m; sa = sm; }
            }
            samples.put(a, sa);
            samples.put(b, sb);
            inserted += 2;
        }
        log.append(String.format(Locale.ROOT, "adaptive cycle: %d uniform + %d keyframes around discontinuities%n", count + 1, inserted));

        ClipBuilder clip = new ClipBuilder().duration(period).loop(true);
        for (Map.Entry<Float, Sample> entry : samples.entrySet())
        {
            clip.frame(entry.getKey(), entry.getValue().rotations, entry.getValue().vectors);
        }
        return clip;
    }

    private static double maxJump(Sample a, Sample b)
    {
        double worst = 0;
        for (String bone : a.rotations.keySet())
        {
            Quaternion qb = b.rotations.get(bone);
            if (qb == null) continue;
            worst = Math.max(worst, PoseComparator.angleBetweenDeg(arr(a.rotations.get(bone)), arr(qb)));
        }
        return worst;
    }

    /**
     * Samples the pose a bit snaps to in {@code onPlay}: only bones whose smoothed value changed
     * (i.e. {@code orientInstant} / {@code identity} calls), not bones that merely got a new target.
     */
    public Sample sampleEnter(AnimationBit<?> bit, Consumer<BakeRig> setup)
    {
        rig.resetPoseToMarker();
        setup.accept(rig);
        rig.play(bit);
        Map<String, Quaternion> snapped = new LinkedHashMap<>();
        for (Map.Entry<String, Quaternion> entry : rig.rotationSmooth().entrySet())
        {
            if (!BakeRig.isMarker(entry.getValue()))
            {
                snapped.put(entry.getKey(), entry.getValue());
            }
        }
        return new Sample(snapped, new LinkedHashMap<>(), rig.smoothness());
    }

    /** {@link #sampleEnter} as a single-keyframe clip. */
    public ClipBuilder sampleEnterClip(AnimationBit<?> bit, Consumer<BakeRig> setup)
    {
        Sample s = sampleEnter(bit, setup);
        return new ClipBuilder().duration(0).loop(false).frame(s.rotations, null);
    }

    /**
     * Samples a cycle of {@code period} over {@code count} intervals (count + 1 keyframes, the last
     * equal in phase to the first) into a looping clip with the given duration.
     */
    public ClipBuilder cycle(AnimationBit<?> bit, Setup setup, float period, int count)
    {
        ClipBuilder clip = new ClipBuilder().duration(period).loop(true);
        for (int i = 0; i <= count; i++)
        {
            float t = (i == count) ? 0 : period * i / count;
            Sample s = sample(bit, setup, t);
            clip.frame(s.rotations, s.vectors);
        }
        return clip;
    }

    /**
     * Samples a stepped cycle: {@code count} hold intervals of equal length, each sampled at its
     * midpoint (never exactly on a switch point, where float rounding is ambiguous) and keyed at
     * its start time.
     */
    public ClipBuilder stepCycle(AnimationBit<?> bit, Setup setup, float period, int count)
    {
        ClipBuilder clip = new ClipBuilder().duration(period).loop(true).step();
        for (int i = 0; i <= count; i++)
        {
            float start = period * i / count;
            float mid = period * (i % count) / count + period / (2F * count);
            Sample s = sample(bit, setup, mid);
            clip.frame(start, s.rotations, s.vectors);
        }
        return clip;
    }

    /** Samples a one-shot over [0, duration] into a non-looping clip. */
    public ClipBuilder oneShot(AnimationBit<?> bit, Setup setup, float duration, int count)
    {
        ClipBuilder clip = new ClipBuilder().duration(duration).loop(false);
        for (int i = 0; i <= count; i++)
        {
            Sample s = sample(bit, setup, duration * i / count);
            clip.frame(s.rotations, s.vectors);
        }
        return clip;
    }

    /**
     * The POST-space delta between two samplings of the same cycle: {@code base^-1 * full}.
     * With {@code base} at amplitude 0 and {@code full} at amplitude 1 this isolates the part of
     * the pose that scales with the amplitude. Also checks that the pose at amplitude 0.5 equals
     * the half-scaled delta, and logs the worst deviation.
     */
    public ClipBuilder amplitudeDelta(AnimationBit<?> bit, Setup base, Setup full, Setup half, float period, int count)
    {
        ClipBuilder clip = new ClipBuilder().duration(period).loop(true);
        double worst = 0;
        String worstBone = "";
        Quaternion inv = new Quaternion();
        Quaternion delta = new Quaternion();
        Quaternion halfDelta = new Quaternion();
        Quaternion halfExpected = new Quaternion();
        for (int i = 0; i <= count; i++)
        {
            float t = (i == count) ? 0 : period * i / count;
            Sample b = sample(bit, base, t);
            Sample f = sample(bit, full, t);
            Sample h = sample(bit, half, t);
            Map<String, Quaternion> deltas = new LinkedHashMap<>();
            Map<String, Vec3f> vectorDeltas = new LinkedHashMap<>();
            for (String bone : f.rotations.keySet())
            {
                if (!b.rotations.containsKey(bone) || !h.rotations.containsKey(bone)) continue;
                conjugate(b.rotations.get(bone), inv);
                Quaternion.mul(inv, f.rotations.get(bone), delta);
                delta.normalise();
                deltas.put(bone, BakeRig.copy(delta));

                conjugate(b.rotations.get(bone), inv);
                Quaternion.mul(inv, h.rotations.get(bone), halfDelta);
                PoseMath.scale(delta, 0.5F, halfExpected);
                double err = PoseComparator.angleBetweenDeg(arr(halfDelta), arr(halfExpected));
                if (err > worst)
                {
                    worst = err;
                    worstBone = bone;
                }
            }
            for (String vector : f.vectors.keySet())
            {
                Vec3f fv = f.vectors.get(vector);
                Vec3f bv = b.vectors.get(vector);
                vectorDeltas.put(vector, new Vec3f(fv.x - bv.x, fv.y - bv.y, fv.z - bv.z));
            }
            clip.frame(deltas, vectorDeltas);
        }
        log.append(String.format(Locale.ROOT, "amplitude delta: worst half-amplitude nonlinearity %.4f deg on %s%n", worst, worstBone));
        return clip;
    }

    public static void conjugate(Quaternion q, Quaternion dest)
    {
        dest.set(-q.x, -q.y, -q.z, q.w);
    }

    private static float[] arr(Quaternion q)
    {
        return new float[] { q.x, q.y, q.z, q.w };
    }

    public static class Sample
    {
        public final Map<String, Quaternion> rotations;
        public final Map<String, Vec3f> vectors;
        public final Map<String, Float> smoothness;

        Sample(Map<String, Quaternion> rotations, Map<String, Vec3f> vectors, Map<String, Float> smoothness)
        {
            this.rotations = rotations;
            this.vectors = vectors;
            this.smoothness = smoothness;
        }
    }
}
