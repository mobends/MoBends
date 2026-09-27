package goblinbob.mobends.core.kumo.state.template.pose;

public class RampTemplate extends DriverItemTemplate
{

    /** Node-local variable name. */
    public String name;

    /** Increase per tick while the condition holds. */
    public float speed = 0.1F;

    /** Decrease per tick while it does not hold; null = same as speed; 0 = never decreases. */
    public Float downSpeed;

    /**
     * When true, the items of this frame read the value as it was before this frame's advance
     * (the bit computed its eased value at the top of perform() and moved the ramp afterwards).
     * The default is to advance first and then expose the value.
     */
    public boolean readBeforeAdvance = false;

    /** Value when the node is entered (0..1). */
    public float initial = 0;

}
