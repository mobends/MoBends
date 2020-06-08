package goblinbob.mobends.core.kumo.state.template.keyframe;

import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

public class ConnectionTemplate
{

    public int targetNodeIndex;
    public TriggerConditionTemplate triggerCondition;

    /**
     * The duration of the transition in ticks.
     */
    public float transitionDuration = 0;

    public Easing transitionEasing = Easing.EASE_IN_OUT;

    public enum Easing
    {
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT,
    }

}
