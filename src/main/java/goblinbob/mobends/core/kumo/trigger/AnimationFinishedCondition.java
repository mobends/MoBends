package goblinbob.mobends.core.kumo.trigger;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.INodeState;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.serial.ISerialInput;

/**
 * @author Iwo Plaza
 */
public class AnimationFinishedCondition<D extends IEntityData> implements ITriggerCondition<D>
{
    @Override
    public boolean isConditionMet(ITriggerConditionContext<D> context)
    {
        INodeState<D> node = context.getCurrentNode();
        if (node != null)
        {
            return node.isAnimationFinished(context);
        }
        return false;
    }

    public static class Template extends TriggerConditionTemplate
    {
        public Template(String type)
        {
            super(type);
        }

        @Override
        public <D extends IEntityData> ITriggerCondition<D> instantiate(IKumoInstancingContext<D> context)
        {
            return new AnimationFinishedCondition<>();
        }

        public static <D extends IEntityData> Template deserialize(ISerialContext<D> context, String type, ISerialInput in)
        {
            return new Template(type);
        }
    }
}
