package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

/**
 * {@code {"core:scale": {...}}}: scales a part from expressions, per axis ({@code x}, {@code y},
 * {@code z}) or on every axis not given its own ({@code all}).
 */
public class ScaleTemplate extends DriverItemTemplate
{

    public String bone;
    public ExpressionTemplate x;
    public ExpressionTemplate y;
    public ExpressionTemplate z;
    public ExpressionTemplate all;

}
