package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

/**
 * {@code {"core:translate": {...}}}: moves a bone from expressions: a part's position offset, or
 * the position of one of the entity's own bones ({@code root}, {@code center}).
 */
public class TranslateTemplate extends DriverItemTemplate
{

    public String bone;
    public ExpressionTemplate x;
    public ExpressionTemplate y;
    public ExpressionTemplate z;

}
