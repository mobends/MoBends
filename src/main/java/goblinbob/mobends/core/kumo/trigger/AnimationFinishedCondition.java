package goblinbob.mobends.core.kumo.trigger;

import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.AnimationRuntimeException;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.keyframe.node.IKeyframeNodeState;

/**
 * @author Iwo Plaza
 */
public class AnimationFinishedCondition<D extends IEntityData> implements ITriggerCondition<D>
{
    @Override
    public boolean isConditionMet(ITriggerConditionContext<D> context)
    {
        try
        {
            IKeyframeNodeState<D> node = (IKeyframeNodeState<D>) context.getCurrentNode();

            if (node != null)
            {
                return node.isAnimationFinished(context);
            }
        }
        catch(ClassCastException e)
        {
            throw new AnimationRuntimeException("Cannot use an AnimationFinished trigger " +
                    "condition on a node that's not a keyframe node.");
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

        public static <D extends IEntityData, C extends ISerialContext<C, D>> Template deserialize(C context, String type, ISerialInput in)
        {
            return new Template(type);
        }
    }
}
