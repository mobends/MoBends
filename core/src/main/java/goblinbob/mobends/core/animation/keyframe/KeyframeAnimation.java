package goblinbob.mobends.core.animation.keyframe;

import java.util.Map;

public class KeyframeAnimation
{

    /**
     * The version of the clip format this build reads (see {@code FormatVersion}). Bump it whenever
     * the format changes in a way old clips can't be read as they are.
     */
    public static final int FORMAT_VERSION = 2;

    /** The format the file was written for (required in clip files; see {@link #FORMAT_VERSION}). */
    public int formatVersion;

    public Map<String, Bone> bones;

    /**
     * Optional: the clip's length in its own units, the range of the frame that
     * plays it ({@code clipLength} in expressions). Keyframes are evenly spaced over it; for a clip
     * meant to loop the last keyframe coincides with the first.
     */
    public Float duration;
    /** "linear" (default) or "step" (hold each keyframe until the next). */
    public String interpolation;
    /**
     * Optional explicit keyframe times (one per keyframe, ascending, in the clip's time units).
     * Without it keyframes are evenly spaced over the duration. Lets a baked clip place two
     * keyframes right around a discontinuity.
     */
    public float[] times;

}
