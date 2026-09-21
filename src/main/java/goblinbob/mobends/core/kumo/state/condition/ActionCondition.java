package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

/**
 * Met while a node carrying the tag is current on any layer:
 * {@code {"type": "core:action", "tag": "walk"}}. This is how a layer reacts to what another
 * layer is doing (and what bends packs used the controller's action strings for).
 *
 * @author Iwo Plaza
 */
public class ActionCondition implements ITriggerCondition
{

    private final String tag;

    public ActionCondition(Template template) throws MalformedKumoTemplateException
    {
        if (template.tag == null)
        {
            throw new MalformedKumoTemplateException("core:action needs a 'tag'.");
        }
        this.tag = template.tag;
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context)
    {
        return context.isActionActive(tag);
    }

    public static class Template extends TriggerConditionTemplate
    {

        public String tag;

    }

}
