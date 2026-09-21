package goblinbob.mobends.core.kumo.state.template;

import java.util.HashMap;
import java.util.Map;

/**
 * JSON: {"default": 0.3, "rightArm": 0.8, "root": [0.3, 0.6, 0.3]}. A bone that is not listed
 * (and no default) keeps whatever damping it had, exactly like a procedural bit that never calls
 * {@code setSmoothness}.
 */
public class DampingTemplate
{

    public static final String DEFAULT = "default";

    /** Per bone: a single rate, or three per-axis rates for vectors. */
    public Map<String, float[]> entries = new HashMap<>();

    /** Per bone: a rate driven by a variable (e.g. the falling bit ramps its smoothness). */
    public Map<String, ValueTemplate> dynamic = new HashMap<>();

    public float[] forBone(String bone)
    {
        float[] value = entries.get(bone);
        return value != null ? value : entries.get(DEFAULT);
    }

}
