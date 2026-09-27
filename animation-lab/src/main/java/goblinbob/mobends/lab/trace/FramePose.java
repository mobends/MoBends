package goblinbob.mobends.lab.trace;

import java.util.LinkedHashMap;
import java.util.Map;

public class FramePose
{
    public float tick;
    /** Bones (IModelPart) and standalone orientations (renderRotation, centerRotation, item rotations). */
    public Map<String, BonePose> bones = new LinkedHashMap<>();
    /** Standalone smoothed vectors (globalOffset, localOffset). */
    public Map<String, VectorPose> vectors = new LinkedHashMap<>();
}
