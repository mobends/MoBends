package goblinbob.mobends.core.types.selector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Selector conditions by their {@code type}. The {@code core:} ones are built in; addons add their
 * own through {@code AddonAnimationRegistry.registerSelectorCondition}.
 */
public class SelectorConditionRegistry
{

    public static final SelectorConditionRegistry INSTANCE = new SelectorConditionRegistry();

    private final Map<String, ISelectorConditionFactory> factories = new HashMap<>();

    private SelectorConditionRegistry()
    {
        register("core:and", CoreSelectorConditions.And::create);
        register("core:or", CoreSelectorConditions.Or::create);
        register("core:not", CoreSelectorConditions.Not::create);
        register("core:entity_type", CoreSelectorConditions.EntityType::create);
        register("core:player_name", CoreSelectorConditions.PlayerName::create);
        register("core:player_uuid", CoreSelectorConditions.PlayerUuid::create);
    }

    public void register(String type, ISelectorConditionFactory factory)
    {
        factories.put(type, factory);
    }

    public ISelectorCondition parse(JsonElement element) throws MalformedKumoTemplateException
    {
        if (element == null || !element.isJsonObject())
        {
            throw new MalformedKumoTemplateException("A selector condition has to be an object.");
        }
        JsonObject json = element.getAsJsonObject();
        JsonElement type = json.get("type");
        if (type == null || !type.isJsonPrimitive())
        {
            throw new MalformedKumoTemplateException("A selector condition needs a 'type'.");
        }
        ISelectorConditionFactory factory = factories.get(type.getAsString());
        if (factory == null)
        {
            throw new MalformedKumoTemplateException("Unknown selector condition '" + type.getAsString() + "'.");
        }
        return factory.create(json, this);
    }

    public List<ISelectorCondition> parseAll(JsonElement array) throws MalformedKumoTemplateException
    {
        if (array == null || !array.isJsonArray())
        {
            throw new MalformedKumoTemplateException("Expected a list of conditions.");
        }
        List<ISelectorCondition> conditions = new ArrayList<>();
        for (JsonElement element : (JsonArray) array)
        {
            conditions.add(parse(element));
        }
        return conditions;
    }

    /** Reads {@code single} (one string) or {@code plural} (a list of strings), whichever is present. */
    public static List<String> strings(JsonObject json, String single, String plural) throws MalformedKumoTemplateException
    {
        List<String> values = new ArrayList<>();
        if (json.has(single))
        {
            values.add(json.get(single).getAsString());
        }
        if (json.has(plural))
        {
            JsonElement list = json.get(plural);
            if (!list.isJsonArray())
            {
                throw new MalformedKumoTemplateException("'" + plural + "' has to be a list.");
            }
            for (JsonElement value : (JsonArray) list)
            {
                values.add(value.getAsString());
            }
        }
        if (values.isEmpty())
        {
            throw new MalformedKumoTemplateException("Expected '" + single + "' or '" + plural + "'.");
        }
        return values;
    }

}
