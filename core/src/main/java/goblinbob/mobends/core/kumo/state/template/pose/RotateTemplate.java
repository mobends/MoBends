package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.util.EnumAxis;

/** {@code {"core:rotate": {...}}}: rotates a bone about one axis by an angle from an expression. */
public class RotateTemplate extends DriverItemTemplate
{

    public String bone;

    public EnumAxis axis;

    /** Angle in degrees. */
    public ExpressionTemplate angle;

}
