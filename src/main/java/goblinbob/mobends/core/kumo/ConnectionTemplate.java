package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.ISerializable;

public class ConnectionTemplate implements ISerializable
{
    public final int targetNodeIndex;
    /**
     * The duration of the transition in ticks.
     */
    public final float transitionDuration;
    public final Easing transitionEasing;
    public final TriggerConditionTemplate triggerCondition;

    public ConnectionTemplate(int targetNodeIndex, float transitionDuration, Easing transitionEasing, TriggerConditionTemplate triggerCondition)
    {
        this.targetNodeIndex = targetNodeIndex;
        this.transitionDuration = transitionDuration;
        this.transitionEasing = transitionEasing;
        this.triggerCondition = triggerCondition;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof ConnectionTemplate))
            return false;
        if (obj == this)
            return true;

        ConnectionTemplate other = (ConnectionTemplate) obj;
        return other.targetNodeIndex == this.targetNodeIndex &&
                other.transitionDuration == this.transitionDuration &&
                other.transitionEasing.equals(this.transitionEasing) &&
                other.triggerCondition.equals(this.triggerCondition);
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        out.writeInt(this.targetNodeIndex);
        out.writeFloat(this.transitionDuration);
        out.writeByte((byte) transitionEasing.ordinal());
        this.triggerCondition.serialize(out);
    }

    public enum Easing
    {
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT,
    }
}
