package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

import java.util.List;

/**
 * A layer's mirroring rule, used by items that set {@code "@mirror": true}: while {@code when}
 * holds, such an item is evaluated as its left-right mirror image (paired bones swapped, Y and Z
 * rotations negated, X offsets negated), so an animation authored for a right-handed entity plays
 * on a left-handed one. Inputs are never negated: an item that follows a world direction (the head
 * turned by {@code headYaw}) isn't mirrored, or only swaps sides.
 */
public class MirrorTemplate
{

    public ExpressionTemplate when;

    /** Pairs of bone names that swap sides, e.g. {@code [["leftArm", "rightArm"], ...]}. */
    public List<List<String>> pairs;

}
