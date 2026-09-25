package goblinbob.mobends.core.types.selector;

import com.google.gson.JsonObject;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

@FunctionalInterface
public interface ISelectorConditionFactory
{

    /**
     * @param json     The condition's JSON object, {@code type} included.
     * @param registry Parses nested conditions.
     */
    ISelectorCondition create(JsonObject json, SelectorConditionRegistry registry) throws MalformedKumoTemplateException;

}
