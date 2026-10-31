package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

/**
 * {@code {"core:translate": {...}}}: moves a bone from expressions: a part's position offset, or
 * one of the entity-level vectors ({@code root} / {@code globalOffset}, {@code localOffset}).
 */
public class TranslateTemplate extends DriverItemTemplate
{

    public String bone;
    public ExpressionTemplate x;
    public ExpressionTemplate y;
    public ExpressionTemplate z;

}
