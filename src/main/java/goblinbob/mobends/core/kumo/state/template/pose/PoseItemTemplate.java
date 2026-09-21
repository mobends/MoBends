package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

/** Common fields of clip and driver items. The concrete class is chosen by the serializer. */
public class PoseItemTemplate
{

    /** Composition space of this item's rotations. Null = OVERRIDE for clips, PRE for drivers. */
    public Pose.Space space;

    /** Optional condition; the item is skipped while it does not hold (the in-bit "if" idiom). */
    public TriggerConditionTemplate when;

}
