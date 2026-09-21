package goblinbob.mobends.standard.kumo.spider;

import goblinbob.mobends.core.kumo.state.template.ValueTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;

/**
 * {@code "driver": "mobends:spider_idle_legs"}: the eight legs keep their feet planted in the
 * world (inverse kinematics from the limbs' remembered positions), step back to a neutral spot
 * when stretched too far, and the front pair "feels" the ground periodically. Exposes the
 * computed ground level as the node variable {@code groundLevel}.
 */
public class SpiderIdleLegsTemplate extends DriverItemTemplate
{

    /** Vertical bob of the body (positive = legs push the body up), in model units. */
    public ValueTemplate groundLevel;
    /** Horizontal sway of the body the legs compensate for. */
    public ValueTemplate bodyX;
    public ValueTemplate bodyZ;

    /** Landing bounce: over {@code kneelDuration} ticks after touchdown, amplitude in model units. */
    public float kneelDuration = 10F;
    public float kneelAmplitude = 4F;
    public float kneelLead = 0F;

    /** How high a foot lifts while it steps to a new spot. */
    public float liftHeight = 4F;

    /** Every {@code feelInterval} ticks of the entity's life, for {@code feelDuration} ticks, the front limbs reach out. */
    public int feelInterval = 100;
    public int feelDuration = 10;
    public int[] feelLimbs = { 6, 7 };
    public float feelX = 0F;
    public float feelZ = 1.5F;
    public float feelSpeed = 0.2F;

    /** Layer variable that, when non-zero on node entry, re-plants every foot under the body (and is cleared). */
    public String resetVariable = "resetLimbs";

}
