package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON: {"default": 0.3, "rightArm": 0.8, "root": [0.3, 0.6, 0.3]}. A bone that is not listed
 * (and no default) keeps whatever damping it had.
 */
public class DampingTemplate
{

    public static final String DEFAULT = "default";

    /** Per bone: a single rate, or three per-axis rates for vectors. */
    public Map<String, float[]> entries = new HashMap<>();

    /** Per bone: a rate driven by a variable (e.g. a smoothness that ramps up while falling). */
    public Map<String, ExpressionTemplate> dynamic = new HashMap<>();

}
