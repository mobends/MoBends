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

    /** Optional condition; the item is skipped while it does not hold (the in-bit "if" idiom). */
    public TriggerConditionTemplate when;

    /** Damping for the bones this item writes (the setSmoothness next to the orient call). */
    public DampingTemplate damping;

    /** Vector modes for the vectors this item writes, e.g. {"root": "SLIDE"}. */
    public Map<String, IVectorSink.Mode> vectorModes;

    /** The bones this item writes jump to their target every frame (the per-frame {@code orientInstant} idiom). */
    public boolean snap;
    /** Evaluate as the mirror image while the layer's mirror condition holds (see the layer's {@code mirror}). */
    public boolean mirror;
    /**
     * Like {@code mirror}, but only the paired bones swap sides: rotations and inputs are kept
     * (a part the bit applies to its main hand without the hand multiplier, e.g. a breathing sway).
     */
    public boolean swapSides;

}
