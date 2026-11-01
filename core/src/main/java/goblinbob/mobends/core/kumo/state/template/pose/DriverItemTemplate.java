package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.pose.Pose;

/** Base of driver items: {@code {"core:rotate": {...}}} picks the concrete template. */
public class DriverItemTemplate extends PoseItemTemplate
{

    public String driver;

    /** Composition space of what the driver writes; null = the driver's default (PRE for {@code core:rotate}). */
    public Pose.Space space;

}
