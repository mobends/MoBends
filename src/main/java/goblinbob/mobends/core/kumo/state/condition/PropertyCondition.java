package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

import java.util.List;

/**
 * Met when a string property of the subject equals the value (or is one of the values):
 * {@code {"type": "core:property", "property": "attackActionType", "value": "SWORD"}}.
 * A null property matches {@code "value": null} / an empty list only via {@code "unset": true}.
 *
 * @author Iwo Plaza
 */
public class PropertyCondition implements ITriggerCondition
{

    private final String property;
    private final List<String> values;
    private final String value;
    private final boolean unset;

    public PropertyCondition(Template template) throws MalformedKumoTemplateException
    {
        if (template.property == null)
        {
            throw new MalformedKumoTemplateException("core:property needs a 'property'.");
        }
        this.property = template.property;
        this.values = template.values;
        this.value = template.value;
        this.unset = template.unset;
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context)
    {
        String actual = context.getSubject().getProperty(property);
        if (actual == null)
        {
            return unset;
        }
        if (value != null && value.equals(actual))
        {
            return true;
        }
        return values != null && values.contains(actual);
    }

    public static class Template extends TriggerConditionTemplate
    {

        public String property;
        public String value;
        public List<String> values;
        public boolean unset;

    }

}
