package goblinbob.mobends.core.definition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.serializer.ScopeSerializers;
import goblinbob.mobends.core.kumo.state.template.DefinitionTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.OnTemplate;
import goblinbob.mobends.core.kumo.state.template.StatementTemplate;
import goblinbob.mobends.core.util.FormatVersion;
import goblinbob.mobends.core.util.GsonResources;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Loads model definitions ({@code <namespace>:bends/models/<name>.json}) through the resource manager. */
public class ModelDefinitions {
    public static final ModelDefinitions INSTANCE = new ModelDefinitions();

    // The entity scope ("@define", "@on") is read as in an animator.
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(DefinitionTemplate.class, new ScopeSerializers.Definition())
            .registerTypeAdapter(OnTemplate.class, new ScopeSerializers.On())
            .registerTypeAdapter(StatementTemplate.class, new ScopeSerializers.Statement())
            .registerTypeAdapter(ExpressionTemplate.class, new ExpressionTemplate.Deserializer())
            .create();

    private final Map<ResourceLocation, EntityModelDefinition> loaded = new HashMap<>();

    public void clearCache() {
        loaded.clear();
    }

    public static ResourceLocation locationOf(String modId, String name) {
        return ResourceLocation.fromNamespaceAndPath(modId, "bends/models/" + name + ".json");
    }

    /** Loads the definition at {@code location}, e.g. {@code yourmod:bends/models/beast.json}. */
    public EntityModelDefinition load(ResourceLocation location) throws IOException, MalformedKumoTemplateException {
        EntityModelDefinition definition = loaded.get(location);
        if (definition == null) {
            Set<ResourceLocation> visiting = new HashSet<>();
            visiting.add(location);
            try {
                definition = GSON.fromJson(resolve(read(location), visiting), EntityModelDefinition.class);
            } catch (JsonParseException | IllegalStateException e) {
                throw new IOException(location + ": " + e.getMessage(), e);
            }
            definition.validate();
            definition.location = location.toString();
            loaded.put(location, definition);
        }
        return definition;
    }

    private static JsonObject read(ResourceLocation location) throws IOException {
        JsonElement json = GsonResources.read(location, GSON, JsonElement.class);
        try {
            return FormatVersion.check(json, "model definition", EntityModelDefinition.FORMAT_VERSION);
        } catch (JsonParseException e) {
            throw new IOException(location + ": " + e.getMessage(), e);
        }
    }

    /** The definition's JSON, with what it {@code extends} merged in (see {@link DefinitionMerge}). */
    private static JsonObject resolve(JsonObject json, Set<ResourceLocation> visiting) throws IOException, MalformedKumoTemplateException {
        if (!json.has("extends")) {
            return json;
        }
        ResourceLocation parentLocation = ResourceLocation.parse(json.get("extends").getAsString());
        if (!visiting.add(parentLocation)) {
            throw new MalformedKumoTemplateException("The model definition " + parentLocation + " extends itself, through " + visiting + ".");
        }
        return DefinitionMerge.merge(resolve(read(parentLocation), visiting), json);
    }
}
