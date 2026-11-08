package goblinbob.mobends.lab.trace;

import java.util.LinkedHashMap;
import java.util.Map;

public class FramePose
{
    public float tick;
    /** Bones (IModelPart) and standalone orientations (item rotations), and the rotations of the entity's own bones (root, center). */
    public Map<String, BonePose> bones = new LinkedHashMap<>();
    /** The positions of the entity's own bones (root, center). */
    public Map<String, VectorPose> vectors = new LinkedHashMap<>();
}
