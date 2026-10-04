package goblinbob.mobends.standard.kumo.spider;

import java.util.Map;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;

import java.util.List;

/**
 * {@code {"mobends:spider_moving_legs": {...}}}: the walking / crawling gait. Each leg swings
 * between two yaw angles and two stretch distances on a shared clock and is put on the ground by
 * the leg IK. Writes the computed ground level to the state its {@code out} names.
 */
public class SpiderMovingLegsTemplate extends DriverItemTemplate
{

    /** The gait clock in radians (limb swing scaled, or the crawl progress). */
    public ExpressionTemplate swing;
    /** Vertical bob of the body, in model units. */
    public ExpressionTemplate groundLevel;

    /** Optional landing bounce; null = none. */
    public Float kneelDuration;
    public float kneelAmplitude = 3F;
    public float kneelLead = 0.2F;

    /** How high a foot lifts at the top of its step, in model units. */
    public float liftHeight = 4F;

    /** The first time the gait plays, its leg smoothing ramps from 0 to 1 at this rate per tick; afterwards the legs snap. */
    public float startSpeed = 0.1F;

    /** Eight entries, one per leg. */
    public List<Limb> limbs;

    /** A state that, when non-zero on node entry, re-plants every foot under the body (and is cleared): e.g. {@code layer.resetLimbs}. */
    public String reset;

    /** Where the computed ground level goes: {@code {"groundLevel": "node.groundLevel"}}. */
    public Map<String, String> out;

    public static class Limb
    {
        /** Phase offset on the gait clock, radians. */
        public float phase = 0F;
        public float minDist = 10F;
        public float maxDist = 20F;
        public float minRot = 0F;
        public float maxRot = 0F;
    }

}
