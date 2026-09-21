package goblinbob.mobends.core.kumo.state.template;

/** JSON: a number, or {"variable": "limbSwingAmount", "scale": 1, "offset": 0, "min": 0, "max": 1}. */
public class ValueTemplate
{

    public float constant;
    public String variable;
    public float scale = 1;
    public float offset = 0;
    public float min = Float.NEGATIVE_INFINITY;
    public float max = Float.POSITIVE_INFINITY;

}
