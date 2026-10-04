package goblinbob.mobends.core.definition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import goblinbob.mobends.core.client.PackTrust;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.serializer.ScopeSerializers;
import goblinbob.mobends.core.kumo.state.template.DefinitionTemplate;
import goblinbob.mobends.core.kumo.state.template.OnTemplate;
import goblinbob.mobends.core.kumo.state.template.StatementTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.GsonResources;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Loads model definitions ({@code <namespace>:bends/models/<name>.json}), which type files name as their model. */
public class ModelDefinitions
{

    public static final ModelDefinitions INSTANCE = new ModelDefinitions();

    // The entity scope ("@define", "@on") is read as in an animator.
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(DefinitionTemplate.class, new ScopeSerializers.Definition())
            .registerTypeAdapter(OnTemplate.class, new ScopeSerializers.On())
            .registerTypeAdapter(StatementTemplate.class, new ScopeSerializers.Statement())
            .registerTypeAdapter(ExpressionTemplate.class, new ExpressionTemplate.Deserializer())
            .create();

    private final Map<ResourceLocation, EntityModelDefinition> loaded = new LinkedHashMap<>();

    public void clearCache()
    {
        loaded.clear();
    }

    public static ResourceLocation locationOf(String modId, String name)
    {
        return new ResourceLocation(modId, "bends/models/" + name + ".json");
    }

    public EntityModelDefinition load(String modId, String name) throws IOException, MalformedKumoTemplateException
    {
        return load(locationOf(modId, name));
    }

    /**
     * Reads a definition from {@code json} (a model definition file's content), validated; one that
     * {@code extends} another loads that one from the resources.
     */
    public static EntityModelDefinition parse(JsonElement json) throws MalformedKumoTemplateException
    {
        JsonObject resolved;
        try
        {
            resolved = INSTANCE.resolve(check(json), new HashSet<>()).json;
        }
        catch (IOException e)
        {
            throw new MalformedKumoTemplateException("The model definition it extends can't be read: " + e.getMessage());
        }
        EntityModelDefinition definition = GSON.fromJson(resolved, EntityModelDefinition.class);
        definition.validate();
        return definition;
    }

    private static JsonObject check(JsonElement json) throws MalformedKumoTemplateException
    {
        JsonElement checked = goblinbob.mobends.core.util.FormatVersion.check(json, "model definition", EntityModelDefinition.FORMAT_VERSION);
        if (!checked.isJsonObject())
        {
            throw new MalformedKumoTemplateException("A model definition is an object.");
        }
        return checked.getAsJsonObject();
    }

    /** A definition's JSON with what it extends merged in, and whether every file of it is trusted. */
    private static final class Resolved
    {
        final JsonObject json;
        final boolean trusted;

        Resolved(JsonObject json, boolean trusted)
        {
            this.json = json;
            this.trusted = trusted;
        }
    }

    private Resolved resolve(JsonObject json, Set<ResourceLocation> visiting) throws IOException, MalformedKumoTemplateException
    {
        if (!json.has("extends"))
        {
            return new Resolved(json, true);
        }
        ResourceLocation parentLocation = new ResourceLocation(json.get("extends").getAsString());
        if (!visiting.add(parentLocation))
        {
            throw new MalformedKumoTemplateException("The model definition " + parentLocation + " extends itself, through " + visiting + ".");
        }
        Resolved parent = resolve(check(GsonResources.read(parentLocation, GSON, JsonElement.class)), visiting);
        boolean trusted = parent.trusted && PackTrust.opensTrusted(parentLocation);
        return new Resolved(DefinitionMerge.merge(parent.json, json), trusted);
    }

    /** Loads the definition at {@code location}, e.g. {@code yourmod:bends/models/beast.json}. */
    public EntityModelDefinition load(ResourceLocation location) throws IOException, MalformedKumoTemplateException
    {
        EntityModelDefinition definition = loaded.get(location);
        if (definition == null)
        {
            Set<ResourceLocation> visiting = new HashSet<>();
            visiting.add(location);
            Resolved resolved = resolve(check(GsonResources.read(location, GSON, JsonElement.class)), visiting);
            try
            {
                definition = GSON.fromJson(resolved.json, EntityModelDefinition.class);
            }
            catch (com.google.gson.JsonParseException | IllegalStateException e)
            {
                throw new IOException(location + ": " + e.getMessage(), e);
            }
            definition.validate();
            definition.trusted = resolved.trusted && PackTrust.opensTrusted(location);
            loaded.put(location, definition);
        }
        return definition;
    }

}
