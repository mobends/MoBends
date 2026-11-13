package goblinbob.mobends.core.types;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import goblinbob.mobends.core.definition.LoadCondition;
import goblinbob.mobends.core.kumo.option.OptionTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.FormatVersion;

import java.util.Map;

/**
 * An extension file, {@code assets/<namespace>/bends/extensions/<name>.json}: an animator whose
 * layers go on top of the animator of one entity type. Its nodes pose what they write over the
 * layers below; a {@code core:fallthrough} node writes nothing, so the layers below show through.
 *
 * <pre>
 * {
 *   "formatVersion": 2,
 *   "id": "mypack:wave",
 *   "type": "mobends:player",
 *   "animator": "mypack:bends/animators/wave.json"
 * }
 * </pre>
 */
public class ExtensionDefinition {
    /** The {@code formatVersion} an extension file has to have (see {@link FormatVersion}). */
    public static final int FORMAT_VERSION = 2;

    private static final Gson GSON = new Gson();

    /** Identifies the extension; extensions of one type apply in id order. Two with one id: the last one found wins. */
    public String id;

    /** The id of the entity type it extends, a type file's id (Mo' Bends' are their models' keys, such as {@code mobends:zombie}). */
    public String type;

    /** The animator whose layers are added. */
    public String animator;

    /** Optional: options the user sets in the Mo' Bends menu, read by the extension's animator as {@code option.x} (see {@link OptionTemplate}). */
    public Map<String, OptionTemplate> options;

    /**
     * Optional: a condition known when the file loads (constant operations only, such as
     * {@code {"core:minecraft_version": [">=", "1.10"]}}); while it doesn't hold, the extension is left
     * out, as if the file weren't there. For a mob some Minecraft versions don't have.
     */
    @SerializedName("@when")
    public JsonElement when;

    /** Whether its {@link #when} holds; computed by {@link #validate}. */
    private transient boolean applies = true;

    public static ExtensionDefinition parse(JsonElement json) throws MalformedKumoTemplateException {
        ExtensionDefinition definition;
        try {
            definition = GSON.fromJson(FormatVersion.check(json, "extension file", FORMAT_VERSION), ExtensionDefinition.class);
        } catch (JsonParseException | IllegalStateException e) {
            throw new MalformedKumoTemplateException("Not a valid extension file: " + e.getMessage());
        }
        definition.validate();
        return definition;
    }

    public void validate() throws MalformedKumoTemplateException {
        if (id == null || id.isEmpty()) {
            throw new MalformedKumoTemplateException("An extension needs an 'id'.");
        }
        if (type == null || type.isEmpty()) {
            throw new MalformedKumoTemplateException("The extension '" + id + "' needs the 'type' it extends.");
        }
        if (animator == null || animator.isEmpty()) {
            throw new MalformedKumoTemplateException("The extension '" + id + "' needs an 'animator'.");
        }
        OptionTemplate.validate(options, "the extension '" + id + "'");
        applies = when == null || LoadCondition.holds(when, "The extension '" + id + "'");
    }

    /** Whether the extension applies on this Minecraft version: its {@code @when} holds, or it has none. */
    public boolean applies() {
        return applies;
    }
}
