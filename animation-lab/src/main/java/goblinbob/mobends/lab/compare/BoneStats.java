package goblinbob.mobends.lab.compare;

/** Error statistics for one bone over a whole trace. */
public class BoneStats
{
    public final String bone;
    public double maxAngleDeg;
    public int maxAngleFrame = -1;
    public double sumAngleDeg;
    public double maxOffset;
    public int maxOffsetFrame = -1;
    public double sumOffset;
    public int frames;

    public BoneStats(String bone)
    {
        this.bone = bone;
    }

    public double meanAngleDeg()
    {
        return frames == 0 ? 0 : sumAngleDeg / frames;
    }

    public double meanOffset()
    {
        return frames == 0 ? 0 : sumOffset / frames;
    }

    void addAngle(double deg, int frame)
    {
        sumAngleDeg += deg;
        if (deg > maxAngleDeg)
        {
            maxAngleDeg = deg;
            maxAngleFrame = frame;
        }
    }

    void addOffset(double dist, int frame)
    {
        sumOffset += dist;
        if (dist > maxOffset)
        {
            maxOffset = dist;
            maxOffsetFrame = frame;
        }
    }
}
