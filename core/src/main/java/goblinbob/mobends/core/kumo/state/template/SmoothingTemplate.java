package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON: {"@default": 3, "rightArm": 1.25, "root": [3, 1.7, 3]}: the ticks a bone takes to reach its
 * target, 0 for at once. A bone nothing smooths in a frame gets its base smoothing (the model definition's, or 0), not what it was given last.
 * A bone's rotation and position can be smoothed apart: {"root": {"rotation": 1.67, "position": [3, 1.7, 3]}}.
 */
public class SmoothingTemplate
{

    public static final String DEFAULT = "@default";

    /** Per bone: a single smoothing, or three per-axis ones for vectors (NaN: that axis keeps its own). */
    public Map<String, float[]> entries = new HashMap<>();

    /** Per bone: a smoothing for its rotation alone (one value). */
    public Map<String, float[]> rotations = new HashMap<>();

    /** Per bone: a smoothing for its position alone (one, or three per-axis ones; NaN: that axis keeps its own). */
    public Map<String, float[]> positions = new HashMap<>();

    /** Per bone: a smoothing computed every frame (e.g. one that shrinks as a fall goes on). */
    public Map<String, ExpressionTemplate> dynamic = new HashMap<>();

}
