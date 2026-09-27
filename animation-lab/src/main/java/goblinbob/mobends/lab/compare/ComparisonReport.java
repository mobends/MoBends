package goblinbob.mobends.lab.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ComparisonReport
{
    public final String title;
    public final Map<String, BoneStats> bones = new TreeMap<>();
    public final Map<String, BoneStats> vectors = new TreeMap<>();
    public final List<String> problems = new ArrayList<>();
    public int frames;

    public ComparisonReport(String title)
    {
        this.title = title;
    }

    public double worstAngleDeg()
    {
        return bones.values().stream().mapToDouble(s -> s.maxAngleDeg).max().orElse(0);
    }

    public double worstOffset()
    {
        double a = bones.values().stream().mapToDouble(s -> s.maxOffset).max().orElse(0);
        double b = vectors.values().stream().mapToDouble(s -> s.maxOffset).max().orElse(0);
        return Math.max(a, b);
    }

    public boolean isWithin(double maxAngleDeg, double maxOffset)
    {
        return problems.isEmpty() && worstAngleDeg() <= maxAngleDeg && worstOffset() <= maxOffset;
    }

    public String toMarkdown()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("### ").append(title).append('\n');
        sb.append(String.format(Locale.ROOT, "frames: %d, worst rotation error: %.4f deg, worst offset error: %.5f%n%n", frames, worstAngleDeg(), worstOffset()));
        for (String problem : problems)
        {
            sb.append("- PROBLEM: ").append(problem).append('\n');
        }
        sb.append("| bone | max rot (deg) | @frame | mean rot (deg) | max offset | @frame |\n");
        sb.append("|---|---:|---:|---:|---:|---:|\n");
        for (BoneStats s : bones.values())
        {
            sb.append(String.format(Locale.ROOT, "| %s | %.4f | %d | %.4f | %.5f | %d |%n",
                    s.bone, s.maxAngleDeg, s.maxAngleFrame, s.meanAngleDeg(), s.maxOffset, s.maxOffsetFrame));
        }
        for (BoneStats s : vectors.values())
        {
            sb.append(String.format(Locale.ROOT, "| %s (vector) | - | - | - | %.5f | %d |%n",
                    s.bone, s.maxOffset, s.maxOffsetFrame));
        }
        return sb.toString();
    }
}
