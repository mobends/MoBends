package goblinbob.mobends.core.types;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

/**
 * An extension file, {@code assets/<namespace>/bends/extensions/<name>.json}: an animator whose
 * layers go on top of the animator of one entity type. Its nodes pose what they write over the
 * layers below; a {@code core:fallthrough} node writes nothing, so the layers below show through.
 *
 * <pre>
 * {
 *   "id": "mypack:wave",
 *   "type": "mobends-player",
 *   "animator": "mypack:bends/animators/wave.json"
 * }
 * </pre>
 */
public class ExtensionDefinition
{

    private static final Gson GSON = new Gson();

    /** Identifies the extension; extensions of one type apply in id order. Two with one id: the first one found wins. */
    public String id;

    /** The id of the entity type it extends (a type file's id, or a built-in type: the model's key, such as {@code mobends-minecraft:zombie}). */
    public String type;

    /** The animator whose layers are added. */
    public String animator;

    public static ExtensionDefinition parse(String json) throws MalformedKumoTemplateException
    {
        ExtensionDefinition definition;
        try
        {
            definition = GSON.fromJson(json, ExtensionDefinition.class);
        }
        catch (JsonParseException e)
        {
            throw new MalformedKumoTemplateException("Not a valid extension file: " + e.getMessage());
        }
        if (definition == null)
        {
            throw new MalformedKumoTemplateException("The extension file is empty.");
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
