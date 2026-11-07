package goblinbob.mobends.core.kumo.option;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.JsonAdapter;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * An option as a file declares it, in its {@code "options"}: a value the user sets in the Mo' Bends
 * menu, which animators read as {@code option.<name>}. Model definitions, type files and extension
 * files declare options.
 *
 * <pre>
 * "options": {
 *   "spinAttack": {"default": true, "label": "mobends.option.spin_attack"},
 *   "swayAmount": {"default": 1, "min": 0, "max": 2, "step": 0.1, "label": "mypack.option.sway"}
 * }
 * </pre>
 *
 * The default's type is the option's: a boolean (a switch) or a number (a slider, which needs
 * {@code min} and {@code max}; {@code step} is optional). {@code label} is a language key; without
 * one, the menu shows the option's name.
 */
@JsonAdapter(OptionTemplate.Deserializer.class)
public final class OptionTemplate
{

    public final boolean isBoolean;
    public final double defaultValue;
    /** For a number: its range, and the step it moves by (0 for any value). */
    public final double min, max, step;
    @Nullable
    public final String label;

    private OptionTemplate(boolean isBoolean, double defaultValue, double min, double max, double step, @Nullable String label)
    {
        this.isBoolean = isBoolean;
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.step = step;
        this.label = label;
    }

    public static OptionTemplate ofBoolean(boolean defaultValue, @Nullable String label)
    {
        return new OptionTemplate(true, defaultValue ? 1 : 0, 0, 1, 1, label);
    }

    public static OptionTemplate ofNumber(double defaultValue, double min, double max, double step, @Nullable String label) throws MalformedKumoTemplateException
    {
        if (!(min <= max))
        {
            throw new MalformedKumoTemplateException(String.format("An option's 'min' (%s) is above its 'max' (%s).", min, max));
        }
        if (defaultValue < min || defaultValue > max)
        {
            throw new MalformedKumoTemplateException(String.format("An option's default (%s) is outside its range, %s to %s.", defaultValue, min, max));
        }
        if (step < 0)
        {
            throw new MalformedKumoTemplateException("An option's 'step' can't be negative.");
        }
        return new OptionTemplate(false, defaultValue, min, max, step, label);
    }

    /** {@code value} as this option takes it: a boolean's 0 or 1, a number in its range and on its steps. */
    public double normalize(double value)
    {
        if (isBoolean)
        {
            return value != 0 ? 1 : 0;
        }
        if (Double.isNaN(value))
        {
            return defaultValue;
        }
        if (step > 0)
        {
            value = min + Math.round((value - min) / step) * step;
        }
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Checks the names of a file's options ({@code null} declares none): a name is what follows
     * {@code option.}, so it has no dot.
     */
    public static void validate(@Nullable Map<String, OptionTemplate> options, String where) throws MalformedKumoTemplateException
    {
        if (options == null)
        {
            return;
        }
        for (Map.Entry<String, OptionTemplate> entry : options.entrySet())
        {
            String name = entry.getKey();
            if (name.isEmpty() || name.indexOf('.') >= 0)
            {
                throw new MalformedKumoTemplateException(String.format("An option's name can't be empty or contain a dot: '%s' (in %s).", name, where));
            }
            if (entry.getValue() == null)
            {
                throw new MalformedKumoTemplateException(String.format("The option '%s' (in %s) is null: an option is an object with a \"default\".", name, where));
            }
        }
    }

    /** Reads an option, refusing a key it doesn't take (as every Mo' Bends file does). */
    public static final class Deserializer implements JsonDeserializer<OptionTemplate>
    {
        @Override
        public OptionTemplate deserialize(JsonElement json, Type type, JsonDeserializationContext context)
        {
            try
            {
                return read(json);
            }
            catch (MalformedKumoTemplateException e)
            {
                throw new JsonParseException(e.getMessage());
            }
        }

        private static OptionTemplate read(JsonElement json) throws MalformedKumoTemplateException
        {
            if (!json.isJsonObject())
            {
                throw new MalformedKumoTemplateException("An option is an object with a \"default\", not " + json + ".");
            }
            JsonObject object = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet())
            {
                switch (entry.getKey())
                {
                    case "default": case "min": case "max": case "step": case "label": case "@comment":
                        break;
                    default:
                        throw new MalformedKumoTemplateException(String.format("An option takes no '%s' (it takes \"default\", \"min\", \"max\", \"step\" and \"label\").", entry.getKey()));
                }
            }
            JsonElement defaultValue = object.get("default");
            if (defaultValue == null || !defaultValue.isJsonPrimitive())
            {
                throw new MalformedKumoTemplateException("An option needs a \"default\": a boolean or a number.");
            }
            String label = optionalString(object, "label");
            JsonPrimitive primitive = defaultValue.getAsJsonPrimitive();
            if (primitive.isBoolean())
            {
                if (object.has("min") || object.has("max") || object.has("step"))
                {
                    throw new MalformedKumoTemplateException("A boolean option has no \"min\", \"max\" or \"step\".");
                }
                return ofBoolean(primitive.getAsBoolean(), label);
            }
            if (!primitive.isNumber())
            {
                throw new MalformedKumoTemplateException("An option's \"default\" is a boolean or a number, not " + primitive + ".");
            }
            if (!object.has("min") || !object.has("max"))
            {
                throw new MalformedKumoTemplateException("A number option needs a \"min\" and a \"max\".");
            }
            return ofNumber(primitive.getAsDouble(), number(object, "min"), number(object, "max"),
                    object.has("step") ? number(object, "step") : 0, label);
        }

        private static double number(JsonObject object, String key) throws MalformedKumoTemplateException
        {
            JsonElement value = object.get(key);
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber())
            {
                throw new MalformedKumoTemplateException(String.format("An option's \"%s\" is a number, not %s.", key, value));
            }
            return value.getAsDouble();
        }

        @Nullable
        private static String optionalString(JsonObject object, String key) throws MalformedKumoTemplateException
        {
            JsonElement value = object.get(key);
            if (value == null)
            {
                return null;
            }
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
            {
                throw new MalformedKumoTemplateException(String.format("An option's \"%s\" is a string, not %s.", key, value));
            }
            return value.getAsString();
        }
    }

}
