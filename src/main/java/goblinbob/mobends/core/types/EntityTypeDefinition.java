package goblinbob.mobends.core.types;

import goblinbob.mobends.core.types.selector.SelectorExpression;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import goblinbob.mobends.core.kumo.option.OptionTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.FormatVersion;

import java.util.Map;

/**
 * A type file, {@code assets/<namespace>/bends/types/<name>.json}: which model and animator an
 * entity gets while the selector holds for it.
 *
 * <pre>
 * {
 *   "formatVersion": 2,
 *   "id": "mobends:example_notch",
 *   "selector": {"and": [{"core:entity_type": ["minecraft:player"]}, {"core:player_name": ["Notch"]}]},
 *   "animator": "mobends:bends/animators/example_zombie_walk.json"
 * }
 * </pre>
 */
public class EntityTypeDefinition
{

    /** The model value that keeps the entity vanilla. */
    public static final String VANILLA_MODEL = "vanilla";

    /** The {@code formatVersion} a type file has to have (see {@link FormatVersion}). */
    public static final int FORMAT_VERSION = 2;

    private static final Gson GSON = new Gson();

    /** Identifies the type (ranks are stored by it); two types with one id: the higher-priority pack's wins. */
    public String id;

    /** A boolean expression over the selector-safe operations (see {@link SelectorExpression}); when it's absent the type applies to every entity. */
    public JsonElement selector;

    /**
     * Optional: a bender key ({@code mobends:player}), a model definition
     * ({@code yourmod:bends/models/beast.json}) or {@value #VANILLA_MODEL}. Absent: the model the
     * entity has by default.
     */
    public String model;

    /**
     * Optional: {@code true} makes the model the default model of the entities of the class its
     * definition names, and of its subclasses (as an addon's bender is), and the type applies to the
     * entities it's the default model of; it then has no {@link #selector}. Mo' Bends' own mobs are
     * given so ({@code mobends:zombie} the husk too).
     */
    public boolean defaultModel;

    /** Optional: the animator asset. Absent: the model's own animator. */
    public String animator;

    /**
     * Optional: options the user sets in the Mo' Bends menu, read by the type's animator as
     * {@code option.x}, besides its model definition's (see {@link OptionTemplate}).
     */
    public Map<String, OptionTemplate> options;

    public static EntityTypeDefinition parse(String json) throws MalformedKumoTemplateException
    {
        EntityTypeDefinition definition;
        try
        {
            definition = GSON.fromJson(FormatVersion.check(new JsonParser().parse(json), "type file", FORMAT_VERSION), EntityTypeDefinition.class);
        }
        catch (JsonParseException e)
        {
            throw new MalformedKumoTemplateException("Not a valid type file: " + e.getMessage());
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
        if (defaultModel)
        {
            if (model == null || VANILLA_MODEL.equals(model))
            {
                throw new MalformedKumoTemplateException("The type '" + id + "' is a default model ('defaultModel'), so it needs a 'model'.");
            }
            if (selector != null)
            {
                throw new MalformedKumoTemplateException("The type '" + id + "' is a default model ('defaultModel'): it applies to the entities it's the default model of, so it has no 'selector'.");
            }
        }
        OptionTemplate.validate(options, "the type '" + id + "'");
    }

    public boolean isModelDefinition()
    {
        return model != null && model.endsWith(".json");
    }

    /**
     * The number of conditions of the selector, for precedence (see {@link SelectorExpression#countConditions});
     * a default model's type has one, "this is the entity's default model", as an addon bender's built-in type.
     */
    public int specificity()
    {
        return defaultModel ? 1 : SelectorExpression.countConditions(selector);
    }

}
