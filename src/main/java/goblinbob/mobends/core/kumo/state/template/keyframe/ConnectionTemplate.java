package goblinbob.mobends.core.kumo.state.template.keyframe;

import goblinbob.mobends.core.kumo.state.IKumoValidationContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

public class ConnectionTemplate
{

    /** Legacy: index of the target node in the layer's node array. */
    public int targetNodeIndex = -1;

    /** Format 2: name of the target node. */
    public String target;

    public TriggerConditionTemplate triggerCondition;

    /** The duration of the transition in ticks. */
    public float transitionDuration = 0;

    public Easing transitionEasing = Easing.EASE_IN_OUT;

    public enum Easing
    {
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT,
        /** 1 - e^(-4t): the step response of the procedural smoothing filter. */
        EXPONENTIAL,
    }

    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
        if (triggerCondition == null)
        {
            throw new MalformedKumoTemplateException("No trigger condition has been specified.");
        }

        triggerCondition.validate(context);
    }

}
