package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.state.template.TimeTemplate;
import goblinbob.mobends.core.kumo.state.template.ValueTemplate;

import java.util.List;

public class ClipItemTemplate extends PoseItemTemplate
{

    public String animationKey;

    /** Playback time source; null = the node's elapsed ticks. */
    public TimeTemplate time;

    /** Blend weight; null = 1. Weighting scales rotation angles and offsets. */
    public ValueTemplate weight;

    /** Only these bones of the clip are applied; null = all. */
    public List<String> bones;

    /** Overrides the clip's own duration (time units for one full playthrough). */
    public Float duration;

    /** Overrides the clip's own loop flag. */
    public Boolean loop;

}
