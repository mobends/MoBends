package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

/** {@code "driver": "core:offset"}: sets a bone's position offset (model units) from expressions. */
public class OffsetTemplate extends DriverItemTemplate
{

    public String bone;
    public ExpressionTemplate x;
    public ExpressionTemplate y;
    public ExpressionTemplate z;

}
