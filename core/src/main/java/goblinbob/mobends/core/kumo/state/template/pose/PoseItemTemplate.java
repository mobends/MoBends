package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.bind.IVectorSink;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.state.template.DampingTemplate;

import java.util.Map;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

/** Common fields of clip and driver items. The concrete class is chosen by the serializer. */
public class PoseItemTemplate
{

    /** Composition space of this item's rotations. Null = OVERRIDE for clips, PRE for drivers. */
    public Pose.Space space;

    /** Optional condition; the item is skipped while it does not hold (a ramp goes down instead). */
    public TriggerConditionTemplate when;

    /** Damping for the bones this item writes. */
    public DampingTemplate damping;

    /** Vector modes for the vectors this item writes, e.g. {"root": "SLIDE"}. */
    public Map<String, IVectorSink.Mode> vectorModes;

    /** The bones this item writes jump to their target every frame. */
    public boolean snap;
    /** Evaluate as the mirror image while the layer's mirror condition holds (see the layer's {@code mirror}). */
    public boolean mirror;
    /**
     * Like {@code mirror}, but only the paired bones swap sides: rotations and inputs are kept
     * (e.g. a sway of the main hand that doesn't depend on which hand that is).
     */
    public boolean swapSides;

}
