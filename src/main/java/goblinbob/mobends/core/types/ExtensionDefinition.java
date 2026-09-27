package goblinbob.mobends.core.types;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.FormatVersion;

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
public class ExtensionDefinition
{

    /** The {@code formatVersion} an extension file has to have (see {@link FormatVersion}). */
    public static final int FORMAT_VERSION = 2;

    private static final Gson GSON = new Gson();

    /** Identifies the extension; extensions of one type apply in id order. Two with one id: the first one found wins. */
    public String id;

    /** The id of the entity type it extends (a type file's id, or a built-in type: the model's key, such as {@code mobends:zombie}). */
    public String type;

    /** The animator whose layers are added. */
    public String animator;

    public static ExtensionDefinition parse(String json) throws MalformedKumoTemplateException
    {
        ExtensionDefinition definition;
        try
        {
            definition = GSON.fromJson(FormatVersion.check(new JsonParser().parse(json), "extension file", FORMAT_VERSION), ExtensionDefinition.class);
        }
        catch (JsonParseException e)
        {
            throw new MalformedKumoTemplateException("Not a valid extension file: " + e.getMessage());
        }
        definition.validate();
        return definition;
    }

    public void validate() throws MalformedKumoTemplateException
    {
        if (id == null || id.isEmpty())
        {
            throw new MalformedKumoTemplateException("An extension needs an 'id'.");
        }
        if (type == null || type.isEmpty())
        {
            throw new MalformedKumoTemplateException("The extension '" + id + "' needs the 'type' it extends.");
        }
        if (animator == null || animator.isEmpty())
        {
            throw new MalformedKumoTemplateException("The extension '" + id + "' needs an 'animator'.");
        }
    }

}
