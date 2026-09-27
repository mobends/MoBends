package goblinbob.mobends.core.types;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

/**
 * A type file, {@code assets/<namespace>/bends/types/<name>.json}: which model and animator an
 * entity gets while the selector holds for it.
 *
 * <pre>
 * {
 *   "id": "mobends:example_notch",
 *   "selector": {"type": "core:and", "conditions": [
 *     {"type": "core:entity_type", "entityType": "minecraft:player"},
 *     {"type": "core:player_name", "names": ["Notch"]}
 *   ]},
 *   "animator": "mobends:bends/animators/example_zombie_walk.json"
 * }
 * </pre>
 */
public class EntityTypeDefinition
{

    /** The model value that keeps the entity vanilla. */
    public static final String VANILLA_MODEL = "vanilla";

    private static final Gson GSON = new Gson();

    /** Identifies the type (ranks are stored by it); two types with one id: the first one found wins. */
    public String id;

    /** Optional: when it's absent the type applies to every entity. */
    public JsonObject selector;

    /**
     * Optional: a bender key ({@code mobends-player}), a model definition
     * ({@code yourmod:bends/models/beast.json}) or {@value #VANILLA_MODEL}. Absent: the model the
     * entity has by default.
     */
    public String model;

    /** Optional: the animator asset. Absent: the model's own animator. */
    public String animator;

    public static EntityTypeDefinition parse(String json) throws MalformedKumoTemplateException
    {
        EntityTypeDefinition definition;
        try
        {
            definition = GSON.fromJson(json, EntityTypeDefinition.class);
        }
        catch (JsonParseException e)
        {
            throw new MalformedKumoTemplateException("Not a valid type file: " + e.getMessage());
        }
        if (definition == null)
        {
            throw new MalformedKumoTemplateException("The type file is empty.");
        }
        definition.validate();
        return definition;
    }

    public void validate() throws MalformedKumoTemplateException
    {
        if (id == null || id.isEmpty())
        {
            throw new MalformedKumoTemplateException("A type needs an 'id'.");
        }
    }

    public boolean isModelDefinition()
    {
        return model != null && model.endsWith(".json");
    }

    /** The number of conditions of the selector: every condition that isn't {@code and} / {@code or} / {@code not}. */
    public int specificity()
    {
        return countConditions(selector);
    }

    static int countConditions(JsonElement element)
    {
        if (element == null || !element.isJsonObject())
        {
            return 0;
        }
        JsonObject condition = element.getAsJsonObject();
        String type = condition.has("type") ? condition.get("type").getAsString() : "";
        switch (type)
        {
            case "core:and":
            case "core:or":
                int count = 0;
                JsonElement children = condition.get("conditions");
                if (children != null && children.isJsonArray())
                {
                    for (JsonElement child : (JsonArray) children)
                    {
                        count += countConditions(child);
                    }
                }
                return count;
            case "core:not":
                return countConditions(condition.get("condition"));
            default:
                return 1;
        }
    }

}
