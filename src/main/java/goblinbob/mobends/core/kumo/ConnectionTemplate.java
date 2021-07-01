package goblinbob.mobends.core.kumo;

import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.ISerialOutput;
import goblinbob.bendslib.serial.ISerializable;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.AnimationRuntimeException;
import goblinbob.mobends.core.kumo.trigger.TriggerConditionTemplate;

import java.io.IOException;
import java.util.List;

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

    public <D extends IEntityData> ConnectionState<D> instantiate(List<? extends INodeState<D>> nodes, IKumoInstancingContext<D> context)
    {
        INodeState<D> node = null;

        try
        {
            node = nodes.get(targetNodeIndex);
        }
        catch (IndexOutOfBoundsException ex)
        {
            throw new AnimationRuntimeException(String.format("A connection to node at index: %d was specified, which doesn't exist.",
                    targetNodeIndex));
        }

        if (triggerCondition == null)
        {
            throw new AnimationRuntimeException("No trigger condition was specified for a connection.");
        }

        return new ConnectionState<D>(node,
                triggerCondition.instantiate(context),
                transitionDuration,
                transitionEasing);
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        out.writeInt(this.targetNodeIndex);
        out.writeFloat(this.transitionDuration);
        out.writeByte((byte) transitionEasing.ordinal());
        this.triggerCondition.serialize(out);
    }

    public static <D extends IEntityData, C extends ISerialContext<C, D>> ConnectionTemplate deserialize(C context, ISerialInput in) throws IOException
    {
        int targetNodeIndex = in.readInt();
        float transitionDuration = in.readFloat();
        Easing easing = Easing.values()[in.readByte()];

        return new ConnectionTemplate(
                targetNodeIndex,
                transitionDuration,
                easing,
                TriggerConditionTemplate.deserializeGeneral(context, in)
        );
    }

    public enum Easing
    {
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT,
    }
}
