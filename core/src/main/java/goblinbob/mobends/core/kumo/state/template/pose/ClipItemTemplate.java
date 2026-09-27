package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

import java.util.List;

public class ClipItemTemplate extends PoseItemTemplate
{

    public String animationKey;

    /**
     * Where in the clip it is, in the clip's own units (0 to {@code clipLength}); an expression that
     * can also use {@code clipLength} and {@code duration}. Null = {@code elapsed / duration * clipLength}
     * with a duration, {@code elapsed} without one.
     */
    public ExpressionTemplate frame;

    /** How long the item runs, in ticks; the clip is finished once it has passed. Null = never finishes. */
    public Float duration;

    /** How much of the clip is applied, null = 1: see {@link goblinbob.mobends.core.kumo.pose.ClipBinding#apply}. */
    public ExpressionTemplate weight;

    /** Only these bones of the clip are applied; null = all. */
    public List<String> bones;

}
