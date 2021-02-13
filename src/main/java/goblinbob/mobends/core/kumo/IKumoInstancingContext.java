package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.trigger.ITriggerCondition;

/**
 * This is a context available during template instancing, which should provide all necessary instantiation data.
 *
 * @author Iwo Plaza
 */
public interface IKumoInstancingContext<D extends IEntityData>
{
    KeyframeAnimation getAnimation(String key);

    INodeState<D> createNodeState(IKumoInstancingContext<D> context, KeyframeNodeTemplate nodeTemplate) throws MalformedKumoTemplateException;

    ITriggerCondition<D> createTriggerCondition(TriggerConditionTemplate conditionTemplate, IKumoInstancingContext<D> context) throws MalformedKumoTemplateException;
}
