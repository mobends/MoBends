package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.util.EnumAxis;

public class AxisRotateTemplate extends DriverItemTemplate
{

    public String bone;

    public EnumAxis axis;

    /** Angle in degrees. */
    public ExpressionTemplate angle;

}
