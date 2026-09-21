package goblinbob.mobends.standard.kumo.spider;

import goblinbob.mobends.core.kumo.state.template.ValueTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;

import java.util.List;

/**
 * {@code "driver": "mobends:spider_moving_legs"}: the walking / crawling gait. Each leg swings
 * between two yaw angles and two stretch distances on a shared clock and is put on the ground by
 * the leg IK. Exposes the computed ground level as the node variable {@code groundLevel}.
 */
public class SpiderMovingLegsTemplate extends DriverItemTemplate
{

    /** The gait clock in radians (limb swing scaled, or the crawl progress). */
    public ValueTemplate swing;
    /** Vertical bob of the body, in model units. */
    public ValueTemplate groundLevel;

    /** Optional landing bounce; null = none. */
    public Float kneelDuration;
    public float kneelAmplitude = 3F;
    public float kneelLead = 0.2F;

    /** The first time the gait plays, its leg smoothing ramps from 0 to 1 at this rate per tick; afterwards the legs snap. */
    public float startSpeed = 0.1F;

    /** Eight entries, one per leg. */
    public List<Limb> limbs;

    public String resetVariable = "resetLimbs";

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
