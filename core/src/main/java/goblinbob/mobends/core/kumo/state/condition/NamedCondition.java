package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

/**
 * A named condition (declared in a {@code conditions} object), written as its name where a condition
 * goes: {@code "jumping"}, or {@code {"type": "core:named", "name": "jumping"}}. Each use is a new
 * instance of the declared condition (see {@link ExpressionScope#createCondition}).
 */
public final class NamedCondition
{

    private NamedCondition() {}

    public static final String TYPE = "core:named";

    static ITriggerCondition create(Template template, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        if (template.name == null)
        {
            throw new MalformedKumoTemplateException("core:named needs a 'name'.");
        }
        ITriggerCondition condition = scope.createCondition(template.name);
        if (condition == null)
        {
            throw new MalformedKumoTemplateException(String.format("No condition is named '%s'.", template.name));
        }
        return condition;
    }

    public static class Template extends TriggerConditionTemplate
    {

        public String name;

        public Template() {}

        public Template(String name)
        {
            this.type = TYPE;
            this.name = name;
        }

    }

}
