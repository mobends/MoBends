package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

/**
 * This condition is met once the subject is in the provided state (e.g. ON_GROUND, AIRBORNE).
 * States are looked up by name on the subject, so entity data classes and addons can add their
 * own without touching this class.
 *
 * @author Iwo Plaza
 */
public class StateCondition implements ITriggerCondition
{

    private final String state;

    public StateCondition(Template template) throws MalformedKumoTemplateException
    {
        if (template.state == null)
        {
            throw new MalformedKumoTemplateException("No 'state' property given for trigger condition.");
        }

        this.state = template.state;
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context) throws MalformedKumoTemplateException
    {
        IKumoSubject subject = context.getSubject();

        if (!subject.hasState(state))
        {
            throw new MalformedKumoTemplateException(String.format("Unknown state '%s' for this subject.", state));
        }

        return subject.getState(state);
    }

    public static class Template extends TriggerConditionTemplate
    {

        public String state;

    }

    /** The states every {@code EntityData} provides. Listed here for reference and tooling. */
    public enum State
    {
        ON_GROUND,
        AIRBORNE,
        SPRINTING,
        STANDING_STILL,
        MOVING_HORIZONTALLY,
        SNEAKING,
        IN_WATER,
        UNDERWATER,
        RIDING,
        ALIVE,
    }

}
