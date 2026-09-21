package goblinbob.mobends.core.kumo.state.template.pose;

public class RampTemplate extends DriverItemTemplate
{

    /** Node-local variable name. */
    public String name;

    /** Increase per tick while the condition holds. */
    public float speed = 0.1F;

    /** Decrease per tick while it does not hold; null = same as speed; 0 = never decreases. */
    public Float downSpeed;

}
