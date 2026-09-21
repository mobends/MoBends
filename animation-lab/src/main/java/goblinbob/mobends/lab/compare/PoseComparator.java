package goblinbob.mobends.lab.compare;

import goblinbob.mobends.lab.trace.BonePose;
import goblinbob.mobends.lab.trace.FramePose;
import goblinbob.mobends.lab.trace.PoseTrace;
import goblinbob.mobends.lab.trace.VectorPose;

import java.util.Map;

/**
 * Compares two traces frame by frame. Rotation error is the angle between the two smoothed
 * quaternions (sign-insensitive); offset error is the euclidean distance of the animation offsets.
 */
public class PoseComparator
{
    public static ComparisonReport compare(String title, PoseTrace expected, PoseTrace actual)
    {
        ComparisonReport report = new ComparisonReport(title);

        if (expected.frames.size() != actual.frames.size())
        {
            report.problems.add(String.format("frame count differs: expected %d, actual %d", expected.frames.size(), actual.frames.size()));
        }

        int frames = Math.min(expected.frames.size(), actual.frames.size());
        report.frames = frames;

        for (int i = 0; i < frames; i++)
        {
            FramePose e = expected.frames.get(i);
            FramePose a = actual.frames.get(i);

            for (Map.Entry<String, BonePose> entry : e.bones.entrySet())
            {
                BonePose eb = entry.getValue();
                BonePose ab = a.bones.get(entry.getKey());
                BoneStats stats = report.bones.computeIfAbsent(entry.getKey(), BoneStats::new);
                stats.frames++;
                if (ab == null)
                {
                    if (i == 0) report.problems.add("bone missing in actual trace: " + entry.getKey());
                    continue;
                }
                stats.addAngle(angleBetweenDeg(eb.r, ab.r), i);
                if (eb.o != null && ab.o != null)
                {
                    stats.addOffset(distance(eb.o, ab.o), i);
                }
            }

            for (String name : a.bones.keySet())
            {
                if (!e.bones.containsKey(name) && i == 0)
                {
                    report.problems.add("bone missing in expected trace: " + name);
                }
            }

            for (Map.Entry<String, VectorPose> entry : e.vectors.entrySet())
            {
                VectorPose ev = entry.getValue();
                VectorPose av = a.vectors.get(entry.getKey());
                BoneStats stats = report.vectors.computeIfAbsent(entry.getKey(), BoneStats::new);
                stats.frames++;
                if (av == null)
                {
                    if (i == 0) report.problems.add("vector missing in actual trace: " + entry.getKey());
                    continue;
                }
                stats.addOffset(distance(ev.v, av.v), i);
            }
        }

        return report;
    }

    /**
     * Angle in degrees between two quaternions, ignoring the q / -q ambiguity. Uses
     * 2 * atan2(|a - b|, |a + b|) (after sign alignment), which stays accurate near identity where
     * the usual acos(dot) formula loses all precision.
     */
    public static double angleBetweenDeg(float[] a, float[] b)
    {
        double la = Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2] + a[3] * a[3]);
        double lb = Math.sqrt(b[0] * b[0] + b[1] * b[1] + b[2] * b[2] + b[3] * b[3]);
        if (la == 0 || lb == 0)
        {
            // A zero quaternion renders as a collapsed (all zero) matrix; treat it as maximally wrong
            // unless both are zero.
            return la == lb ? 0 : 180;
        }
        double dot = a[0] * b[0] + a[1] * b[1] + a[2] * b[2] + a[3] * b[3];
        double sign = dot < 0 ? -1 : 1;
        double dx = 0, sx = 0;
        for (int i = 0; i < 4; i++)
        {
            double ai = a[i] / la;
            double bi = sign * b[i] / lb;
            dx += (ai - bi) * (ai - bi);
            sx += (ai + bi) * (ai + bi);
        }
        return Math.toDegrees(2.0 * Math.atan2(Math.sqrt(dx), Math.sqrt(sx)));
    }

    public static double distance(float[] a, float[] b)
    {
        double dx = a[0] - b[0], dy = a[1] - b[1], dz = a[2] - b[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
