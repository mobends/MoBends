package goblinbob.mobends.lab.trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PoseTrace
{
    public int formatVersion = 1;
    public String entity;
    public String scenario;
    public int fps;
    public List<FramePose> frames = new ArrayList<>();

    public PoseTrace()
    {
    }

    public PoseTrace(String entity, String scenario, int fps)
    {
        this.entity = entity;
        this.scenario = scenario;
        this.fps = fps;
    }

    /**
     * Drops rest position / global offset / scale values that did not change since the previous
     * frame (they almost never do). {@link #expand()} restores them.
     */
    public void compact()
    {
        for (int i = frames.size() - 1; i > 0; i--)
        {
            FramePose current = frames.get(i);
            FramePose previous = frames.get(i - 1);
            for (Map.Entry<String, BonePose> entry : current.bones.entrySet())
            {
                BonePose prev = previous.bones.get(entry.getKey());
                if (prev == null) continue;
                BonePose cur = entry.getValue();
                if (Arrays.equals(cur.p, prev.p)) cur.p = null;
                if (Arrays.equals(cur.g, prev.g)) cur.g = null;
                if (Arrays.equals(cur.s, prev.s)) cur.s = null;
            }
        }
    }

    /** Inverse of {@link #compact()}. */
    public void expand()
    {
        for (int i = 1; i < frames.size(); i++)
        {
            FramePose current = frames.get(i);
            FramePose previous = frames.get(i - 1);
            for (Map.Entry<String, BonePose> entry : current.bones.entrySet())
            {
                BonePose prev = previous.bones.get(entry.getKey());
                if (prev == null) continue;
                BonePose cur = entry.getValue();
                if (cur.p == null) cur.p = prev.p;
                if (cur.g == null) cur.g = prev.g;
                if (cur.s == null) cur.s = prev.s;
            }
        }
    }
}
