package goblinbob.mobends.core.kumo.state.template;

import java.util.List;

/**
 * A layer's mirroring rule, used by items that set {@code "mirror": true}: while {@code when}
 * holds, such an item is evaluated as its left-right mirror image (paired bones swapped, Y and Z
 * rotations negated, X offsets negated) with the listed yaw-like input variables negated, so an
 * animation authored for a right-handed entity plays on a left-handed one.
 */
public class MirrorTemplate
{

    public TriggerConditionTemplate when;

    /** Pairs of bone names that swap sides, e.g. {@code [["leftArm", "rightArm"], ...]}. */
    public List<List<String>> pairs;

    /** Variables that are world directions (yaws), negated while a mirrored item is evaluated. */
    public List<String> negate;

}
