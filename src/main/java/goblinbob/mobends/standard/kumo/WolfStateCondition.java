package goblinbob.mobends.standard.kumo;

import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;
import goblinbob.mobends.standard.data.WolfData;

/**
 * This condition is met once the wolf is in the provided state.
 * (e.g. SITTING, etc.)
 *
 * @author Iwo Plaza
 */
public class WolfStateCondition implements ITriggerCondition
{

    private final WolfStateCondition.State state;

    public WolfStateCondition(WolfStateCondition.Template template) throws MalformedKumoTemplateException
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
        WolfData wolfData;

        try
        {
            wolfData = (WolfData) context.getEntityData();
        }
        catch (ClassCastException e)
        {
            throw new MalformedKumoTemplateException("A wolf_state trigger condition was used on something other than a wolf.");
        }

        switch (this.state)
        {
            case SITTING:
                return wolfData.isSitting();
            default:
                return false;
        }
    }

    public static class Template extends TriggerConditionTemplate
    {

        public WolfStateCondition.State state;

    }

    public enum State
    {
        SITTING,
    }

}
